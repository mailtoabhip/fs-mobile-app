package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import android.Manifest
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentYourRewardsBinding
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsBaseFragment
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject
import androidx.recyclerview.widget.DividerItemDecoration
import com.delhivery.axle.data.yourrewards.YourRewardsItemData
import com.delhivery.axle.data.yourrewards.YourRewardsItemDataAction_DownloadProof
import com.delhivery.axle.data.yourrewards.YourRewardsItemDataAction_ViewDetails
import com.delhivery.axle.data.yourrewards.YourRewardsTimeOutAction
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.api.response.FileData
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

class YourRewardsFragment : ShareRateGetRewardsBaseFragment<FragmentYourRewardsBinding, YourRewardsFragmentViewModel>(),YourRewardsAdapterInterface,OnDateSetListener, DocumentUtils.DocumentListInterface {

  var isLoadingData = true
  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true
  companion object {
    /* singleton instance */
    val _instance: YourRewardsFragment by lazy { YourRewardsFragment() }
  }

  override fun getViewModelClass() = YourRewardsFragmentViewModel::class.java

  override fun layoutId() = R.layout.fragment_your_rewards

  private val adapter: YourRewardsRVAdapter by lazy {
    YourRewardsRVAdapter(this)
  }


  @Inject
  lateinit var userPrefs: UserPrefs

  @Inject
  lateinit var dialogUtils: DialogUtils

  @Inject
  lateinit var documentUtils: DocumentUtils

  // Map s3_path -> original URL to handle concurrent downloads
  private val s3PathToUrlMap: HashMap<String, String> = HashMap()
  private var currentDownloadUrl: String? = null

  var dateSelected = "start_date"

  private var calendar: Calendar = Calendar.getInstance()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("YourRewardsFragment_SetupTime")
    fragmentSetupTrace?.start()
    analyticsUtil.moEngageTrackEvent(
            EVENT_VIEW_PAYOUT,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO),
            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy")
    )

    binding.rewardsRv.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@YourRewardsFragment.adapter
      val dividerItemDecoration = DividerItemDecoration(
        getContext(),
        DividerItemDecoration.VERTICAL
      )
      addItemDecoration(dividerItemDecoration)
      addOnScrollListener(PaginationInterface())
    }

    viewModel.userRewardsData.reobserve(viewLifecycleOwner,
      Observer { it?.let { _items -> adapter.operation(_items) }})

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
      isLoadingData = it ?: false
      binding.refreshLayout.isRefreshing = false
    })
    binding.refreshLayout.setOnRefreshListener { refreshData()}

    binding.startDate.setOnClickListener {
      dateSelected = "start_date"
      var dateStr =  binding.startDate.text.toString()
      var curFormater = SimpleDateFormat("dd/MM/yyyy")
      var dateObj = curFormater.parse(dateStr);
      var cal = Calendar.getInstance()
      cal .setTime(dateObj)
      dialogUtils.datePicker(this,cal,minDate = RewardStartDateCalender,maxDate = RewardStartDateCalender)
    }

    binding.endDate.setOnClickListener {
      dateSelected = "end_date"
      var dateStr =  binding.endDate.text.toString()
      var curFormater = SimpleDateFormat("dd/MM/yyyy")
      var dateObj = curFormater.parse(dateStr);
      var cal = Calendar.getInstance()
      cal .setTime(dateObj)
      dialogUtils.datePicker(this,cal,minDate = RewardStartDateCalender,maxDate = RewardStartDateCalender )
    }
    binding.clearAll.setOnClickListener {
      binding.startDate.text =RewardStartDate
      binding.endDate.text= DateUtils.presentTimeInSlashFormat()
      viewModel.startDate = DateUtils.getISToUtcFormatDate(RewardStartDate)
      viewModel.endDate = DateUtils.getISToUtcFormatDate(DateUtils.presentTimeInSlashFormat())
      refreshData()
    }
    binding.startDate.text = RewardStartDate
    binding.endDate.text= DateUtils.presentTimeInSlashFormat()
    viewModel.startDate = DateUtils.getISToUtcFormatDate(RewardStartDate)
    viewModel.endDate = DateUtils.getISToUtcFormatDate(DateUtils.presentTimeInSlashFormat())
    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }
  }

  override fun refreshData() {
    adapter.resetStaticData()
    viewModel.fetchSupplierRewards()
  }

  private fun downloadProofDoc(proofUrl: String) {
     compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      .onBackground()
      .subscribe { granted, error ->
        if (error == null && granted) {
          uiUtils.showProgress()
          // Extract s3_path from URL
          val s3Path = extractS3PathFromUrl(proofUrl)
          if (s3Path != null) {
            // Store mapping for callback
            s3PathToUrlMap[s3Path] = proofUrl
            currentDownloadUrl = proofUrl
            // Use secure Document API to download
            documentUtils.downloadByS3Path(s3Path, this)
          } else {
            uiUtils.hideProgress()
            uiUtils.showSnackbar("Unable to extract document path")
          }
        } else {
          uiUtils.hideProgress()
          uiUtils.showSnackbar(getString(R.string.storage_permission))
        }
      }
  }

  /**
   * Extract s3_path from full S3 URL
   * Handles URLs like: "https://bucket.s3.region.amazonaws.com/trips/vendor_pod/docket/docket_1764233770054.jpg"
   * Returns: "trips/vendor_pod/docket/docket_1764233770054.jpg"
   */
  private fun extractS3PathFromUrl(docUrl: String): String? {
    return try {
      // Construct AWS base path from config
      val bucket = com.delhivery.axle.config.AWSConfig.Bucket.value()
      val region = com.delhivery.axle.config.AWSConfig.ServerRegion.value()
      val awsBasePath = "https://$bucket.s3.$region.amazonaws.com/"
      
      // Remove base URL from full S3 URL
      if (docUrl.startsWith(awsBasePath)) {
        docUrl.replace(awsBasePath, "").split("?")[0] // Remove query parameters if any
      } else {
        // Fallback: Try to extract path from URL pattern (bucket.s3.region.amazonaws.com/path)
        val parts = docUrl.split(".amazonaws.com/")
        if (parts.size > 1) {
          parts[1].split("?")[0] // Remove query parameters if any
        } else {
          null
        }
      }
    } catch (e: Exception) {
      Log.e("YourRewardsFragment", "Error extracting s3_path: ${e.message}")
      null
    }
  }

  // DocumentListInterface implementation for secure download API
  override fun onDocumentListSuccess(files: List<FileData>) {
    if (files.isNotEmpty()) {
      val documentFile = files.first()
      // Use s3_path from response to find the original URL
      val s3Path = documentFile.s3Path
      val originalUrl = s3Path?.let { s3PathToUrlMap[it] }
      
      Log.d("YourRewardsFragment", "onDocumentListSuccess: s3Path=$s3Path, originalUrl=$originalUrl")
      
      if (originalUrl == null) {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Unable to determine original document URL")
        // Clean up
        s3Path?.let { s3PathToUrlMap.remove(it) }
        currentDownloadUrl = null
        return
      }
      
      // Download file from pre-signed URL
      downloadFileFromUrl(documentFile.downloadUrl, originalUrl)
    } else {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("No documents found")
      // Clean up
      currentDownloadUrl?.let { 
        val s3Path = extractS3PathFromUrl(it)
        s3Path?.let { s3PathToUrlMap.remove(it) }
      }
      currentDownloadUrl = null
    }
  }

  override fun onDocumentListFailure(error: String) {
    uiUtils.hideProgress()
    uiUtils.showSnackbar("Download failed: $error")
    // Clean up mapping
    currentDownloadUrl?.let { 
      val s3Path = extractS3PathFromUrl(it)
      s3Path?.let { s3PathToUrlMap.remove(it) }
    }
    currentDownloadUrl = null
  }

  /**
   * Download file from pre-signed URL using OkHttpClient
   */
  private fun downloadFileFromUrl(downloadUrl: String, originalDocUrl: String) {
    compositeDisposable += io.reactivex.Observable.fromCallable {
      val client = OkHttpClient()
      val request = Request.Builder()
        .url(downloadUrl)
        .build()
      
      val response: Response = client.newCall(request).execute()
      
      if (response.isSuccessful) {
        val file = getFile(originalDocUrl)
        
        if (file != null) {
          response.body()?.byteStream()?.use { input ->
            file.outputStream().use { output ->
              input.copyTo(output)
            }
          }
          file.absolutePath
        } else {
          null
        }
      } else {
        null
      }
    }
    .onBackground()
    .subscribe({ filePath ->
      if (filePath != null) {
        uiUtils.showSnackbar("File downloaded successfully")
      } else {
        uiUtils.showSnackbar("Download failed")
      }
      uiUtils.hideProgress()
      // Clean up mapping
      val s3Path = extractS3PathFromUrl(originalDocUrl)
      s3Path?.let { s3PathToUrlMap.remove(it) }
      currentDownloadUrl = null
    }, { error ->
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Download error: ${error.message}")
      // Clean up mapping on error
      val s3Path = extractS3PathFromUrl(originalDocUrl)
      s3Path?.let { s3PathToUrlMap.remove(it) }
      currentDownloadUrl = null
    })
  }

  private fun getFile(url: String): File? {
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val arrString = url.split("/")
    val fileName = arrString[arrString.size - 1].split("?")[0] // Remove query parameters
    val timestamp = System.currentTimeMillis()
    val file = File(storageDir, "${timestamp}${fileName}")
    // Ensure directory exists
    if (!storageDir.exists()) {
      storageDir.mkdirs()
    }
    return file
  }

  override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
    calendar.set(year, month, day)

  if(dateSelected=="start_date"){
    binding.startDate.text =  String.format("%02d/%02d/%d",  calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR))
    viewModel.startDate = DateUtils.getISToUtcFormatDate(binding.startDate.text.toString())
    refreshData()
   }else{
    binding.endDate.text =  String.format("%02d/%02d/%d",  calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR))
    viewModel.endDate = DateUtils.getISToUtcFormatDate(binding.endDate.text.toString())
    refreshData()

   }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchSupplierRewards(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  override fun handleAction(actionId: String, item: BaseYourRewardsRVAdapterItem<*>,position:Int) {
      when (actionId) {
        YourRewardsItemDataAction_ViewDetails -> {
          val data = item.data as YourRewardsItemData
          data.isFullDetailsEnabled = !data.isFullDetailsEnabled
          adapter.notifyItemChanged(position)

        }
        YourRewardsItemDataAction_DownloadProof -> {
          val data = item.data as YourRewardsItemData
          try {
            // proofUrl is already a complete URL from the backend
            data.proofUrl?.get(0)?.let { url ->
              downloadProofDoc(url)
            }
          }catch (e:Exception){
            uiUtils.showSnackbar("Failed to download proof document")
          }
        }
        YourRewardsTimeOutAction -> {
          refreshData()
        }
      }
  }


}
const val RewardStartDate = "01/07/2022"
const val RewardStartDateCalender = 99999


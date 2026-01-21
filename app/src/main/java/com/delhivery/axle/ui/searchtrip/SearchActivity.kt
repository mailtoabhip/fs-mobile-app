package com.delhivery.axle.ui.searchtrip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchAction_ResetTrip
import com.delhivery.axle.api.request.SearchAction_SearchTrip
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.databinding.ActivitySearchBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.PaginationScrollListener
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import javax.inject.Inject

class SearchActivity : BaseActivity<ActivitySearchBinding, SearchViewModel>(),
    SearchRVAdapterInterface, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface {

  override fun getViewModelClass() = SearchViewModel::class.java

  override fun layoutId() = R.layout.activity_search

  override fun requireConnection() = true

  var isLoadingData = true

  init {
    hasInlineProgress = true
  }

  private val adapter by lazy { SearchRVAdapter(this) }

  @Inject lateinit var documentUtils: DocumentUtils
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  private var currentPodUrl: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("SearchActivity_SetupTime")
    activitySetupTrace?.start()
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
    title = "Search Trips"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (viewModel.total > 0) {
          adapter.resetStaticData()
          viewModel.total = 0
        } else {
        }
        finish()
      }
    })

    binding.rvSearch.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@SearchActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    viewModel.searchLiveData.observe(this, Observer {
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    adapter.resetStaticData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  private fun searchTrips(data: SearchRequest? = null) {
    val vehicleNumber = data?.vehicleNumber
    val lrNumber = data?.lr
    if(!vehicleNumber.isNullOrEmpty() && !lrNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(vehicleNumber = vehicleNumber, lr = lrNumber)
    }else if(!vehicleNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(vehicleNumber = vehicleNumber)
    }else if(!lrNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(lr = lrNumber)
    }else{
      uiUtils.showSnackbar("Please enter search parameters")
      return
    }
    adapter.refresh()
    uiUtils.toggleKeyboard()
    viewModel.searchTrips()
  }

  override fun onDocumentSuccess(downloadUrl: String) {
    // SearchActivity doesn't use upload functionality
    // This method is required by DocumentProgressInterface but not used here
  }

  override fun onDocumentFailure(error: String) {
    // SearchActivity doesn't use upload functionality
    // This method is required by DocumentProgressInterface but not used here
  }

  override fun onDocumentListSuccess(documents: List<FileData>) {
    if (!isFinishing && documents.isNotEmpty()) {
      val documentFile = documents.first()
      val podUrl = currentPodUrl

      if (podUrl != null) {
        val file = getFile(podUrl)
        if (file != null) {
          // Download file from pre-signed URL
          downloadFileFromUrl(documentFile.downloadUrl, podUrl, file)
        } else {
          uiUtils.hideProgress()
          uiUtils.showSnackbar("Can't process POD")
          currentPodUrl = null
        }
      } else {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Download context lost")
        currentPodUrl = null
      }
    } else {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("No documents found")
      currentPodUrl = null
    }
  }

  override fun onDocumentListFailure(error: String) {
    if (!isFinishing) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Couldn't complete download, please try after sometime")
      currentPodUrl = null
    }
  }

  /**
   * Extract s3_path from full S3 URL
   */
  private fun extractS3PathFromUrl(docUrl: String): String? {
    return try {
      // Check if it's already a relative path (no http/https protocol and no .amazonaws.com)
      if (!docUrl.startsWith("http://") && !docUrl.startsWith("https://") && !docUrl.contains(".amazonaws.com")) {
        // Already a relative path, return as-is (remove query parameters if any)
        return docUrl.split("?")[0]
      }
      
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
      Log.e("SearchActivity", "Error extracting s3_path: ${e.message}")
      null
    }
  }

  /**
   * Download file from pre-signed URL using OkHttpClient
   */
  private fun downloadFileFromUrl(downloadUrl: String, originalUrl: String, file: File) {
    compositeDisposable += io.reactivex.Observable.fromCallable {
      val client = OkHttpClient()
      val request = Request.Builder()
        .url(downloadUrl)
        .build()

      val response: Response = client.newCall(request).execute()

      if (response.isSuccessful) {
        response.body()?.byteStream()?.use { input ->
          file.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        file.absolutePath
      } else {
        null
      }
    }
    .onBackground()
    .subscribe({ filePath ->
      if (filePath != null) {
        uiUtils.showSnackbar("Downloaded")
        openFile(file)
      } else {
        uiUtils.showSnackbar("Download failed")
      }
      uiUtils.hideProgress()
      currentPodUrl = null
    }, { error ->
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Download error: ${error.message}")
      currentPodUrl = null
    })
  }

  private fun openFile(file: File) {
    try {
      val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
      val intent = Intent(Intent.ACTION_VIEW)
      if (file.toString().contains(".pdf")) {
        intent.setDataAndType(uri, "application/pdf")
      } else if (file.toString().contains(".jpg") ||
          file.toString().contains(".jpeg") || file.toString().contains(".png")
      ) {
        intent.setDataAndType(uri, "image/jpeg")
      } else {
        intent.setDataAndType(uri, "*/*")
      }
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    } catch (e: java.lang.Exception) {
      uiUtils.showSnackbar("No application found which can open the file")
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchRVAdapterItem<*>
  ) {
    when (actionId) {
      SearchAction_SearchTrip -> {
        val data = item.data as SearchRequest
        searchTrips(data)
      }

      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as HomeTripsItemData
        startActivity(tripDetailsIntent(data.key(), this))
      }

        HomeTripsRequestAction_UploadEpod -> {
            val data = item.data as HomeTripsItemData
            viewModel.transactionId = data.transactionId

            if (data.podUrl.isNullOrEmpty()) {
                // No POD exists, navigate to upload screen
                startActivityForResult(
                    uploadImageIntent(this, data.transactionId, data.reachedTime!!, data.unloadingTime!!),
                    REQCODE_UPLOAD_POD
                )
            } else {
                // POD exists, download it using secure Document API
                uiUtils.showSnackbar("Downloading POD....")
                uiUtils.showProgress()
                viewModel.transactionId = data.transactionId
                // Extract s3_path from podUrl and download
                val s3Path = extractS3PathFromUrl(data.podUrl)
                if (s3Path != null) {
                    currentPodUrl = data.podUrl
                    documentUtils.downloadByS3Path(s3Path, this)
                } else {
                    uiUtils.hideProgress()
                    uiUtils.showSnackbar("Unable to extract document path")
                }
            }
        }

      HomeTripsRequestAction_UploadTracking -> {
        val data = item.data as HomeTripsItemData
        when {
          data.hasPODTracking() ->
            startActivityForResult(
                docketUpdateIntent(context = this, trip = data, podStatus = data.podAction()), REQCODE_UPLOAD_DOCKET
            )
          else -> {
            val list = arrayListOf<String>()
            list.add(data.transactionId)
            startActivityForResult(
                docketUpdateIntent(context = this, transactionIds = list, podStatus = data.podAction()), REQCODE_UPLOAD_DOCKET
            )
          }
        }
      }

      SearchAction_ResetTrip, SearchWarningAction_NoResult -> {
        viewModel.request = SearchRequest()
        adapter.resetStaticData()
        viewModel.total = 0
      }

      SearchTimeOutAction -> {
        searchTrips()
      }
    }
  }

  private fun getFile(podUrl: String?): File? {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val basePath = "$storageDir/" + viewModel.transactionId
    if (podUrl != null) {
      return when {
        podUrl.endsWith("pdf") -> File(basePath + "_pod.pdf")
        podUrl.endsWith("png") -> File(basePath + "_pod.png")
        podUrl.endsWith("jpg") || podUrl.endsWith("jpeg") -> File(basePath + "_pod.jpg")
        else -> null
      }
    }
    return null
  }

/*  override fun onBackPressed() {
    if (viewModel.total > 0) {
      adapter.resetStaticData()
      viewModel.total = 0
    } else {
      super.onBackPressed()
    }
  }*/

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_UPLOAD_POD, REQCODE_UPLOAD_DOCKET -> {
        if (resultCode == Activity.RESULT_OK) {
          searchTrips(viewModel.request)
        }
      }
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(
      UserSearchLimit
  ) {
    override fun loadMore() = viewModel.searchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

}

/**
 * Trip details intent
 */
fun searchIntent(
  context: Context
) = Intent(context, SearchActivity::class.java).apply {
}

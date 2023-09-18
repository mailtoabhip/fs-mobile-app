package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import android.Manifest
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Build
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

class YourRewardsFragment : ShareRateGetRewardsBaseFragment<FragmentYourRewardsBinding, YourRewardsFragmentViewModel>(),YourRewardsAdapterInterface,OnDateSetListener,  AWSUtils.AWSProgressInterface {

  var isLoadingData = true

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

  @Inject lateinit var awsUtils: AWSUtils

  @Inject
  lateinit var dialogUtils: DialogUtils

  var dateSelected = "start_date"

  private var calendar: Calendar = Calendar.getInstance()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    analyticsUtil.trackEvent(
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

    viewModel.delegationDownloadLiveData.reobserve(this, Observer {
      if (it != null) {
        awsUtils.startDownload(it.first, it.second, it.third, this)
      }
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

  override fun refreshData() {
    adapter.resetStaticData()
    viewModel.fetchSupplierRewards()
  }

  private fun downloadProofDoc(item: String) {
     compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      .onBackground()
      .subscribe { granted, error ->
        if (error == null && granted) {
          uiUtils.showProgress()
          val file = getFile(item)
          if (file != null) {
            viewModel.getDownloadDelegationToken(item, file)
          } else {
            uiUtils.showSnackbar("Can't process image")
          }
        } else {
          uiUtils.hideProgress()
          uiUtils.showSnackbar(getString(R.string.storage_permission))
        }
      }
  }

  private fun getFile(item: String): File? {
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    val basePath = "$storageDir/"+System.currentTimeMillis()
    val arrString = item.split("/")
    return File(basePath + arrString[arrString.size - 1])
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
            downloadProofDoc(
             data.proofUrl?.get(0)?.replace(awsUtils.awsBasePath(), "")!!
            )
          }catch (e:Exception){
          }
        }
        YourRewardsTimeOutAction -> {
          refreshData()
        }
      }
  }

  override fun onAWSSuccess(path: String) {
    uiUtils.hideProgress()
    uiUtils.showSnackbar("Document downloaded successfully")

  }

  override fun onAWSFailure() {
      uiUtils.showSnackbar("Document download failed!")
      uiUtils.hideProgress()
  }

}
const val RewardStartDate = "01/07/2022"
const val RewardStartDateCalender = 99999


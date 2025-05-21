package com.delhivery.axle.ui.home.fragments.trips

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.FragmentHomeTripsBinding
import com.delhivery.axle.ui.bids.userTripsIntent
import com.delhivery.axle.ui.dialogs.DownloadLedgerDialog
import com.delhivery.axle.ui.dialogs.LedgerSuccessDialog
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.*
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.searchongoingtrip.searchOngoingTripIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.*
import javax.inject.Inject

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>()
{

  var _title: String = "Ongoing Trips"
  var downloadID = 0.toLong()

  @Inject lateinit var userPrefs: UserPrefs
  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true
  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeTripsFragment by lazy { HomeTripsFragment() }
  }

  @Inject lateinit var awsUtils: AWSUtils

  override fun getViewModelClass() = HomeTripsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_trips

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeTripsFragment_SetupTime")
    fragmentSetupTrace?.start()
    context?.let { ContextCompat.registerReceiver(it,onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),ContextCompat.RECEIVER_NOT_EXPORTED) }
    /**
     Track Event when trip_screen is created
     */
    analyticsUtil.trackEvent(
            EVENT_VIEW_TRIPS,
            mutableListOf(PROPERTY_USER_ID),
            mutableListOf(userPrefs.userId())
    )

    viewModel.fetchTripsSummary()

    viewModel.progressLiveData.reobserve(viewLifecycleOwner, ProgressObserver())

    viewModel.dataLoadingLiveData.observe(viewLifecycleOwner, Observer {
      userPrefs.awaitingArrivalCount=viewModel.awaitingArrivalCount
      uiUtils.hideProgress()
      setText()
    })

    viewModel.downloadPressed.observe(viewLifecycleOwner, Observer {
      requestPermission()
    })

    viewModel.downloadLoadingLiveData.observe(viewLifecycleOwner, Observer {
      downloadLedger(it.url)
      uiUtils.showSnackbar("Ledger downloaded successfully")
    })

    viewModel.emailLoadingLiveData.observe(viewLifecycleOwner, Observer {
      LedgerSuccessDialog(requireContext()).show()
    })

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      uiUtils.showProgress()
      viewModel.fetchTripsSummary()
    }

    binding.downloadContainer.setOnClickListener {
      DownloadLedgerDialog(requireContext(), viewModel,analyticsUtil,userPrefs).show()
    }

    binding.labelViewAllTrips.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(userTripsIntent(requireContext(), "all", 0))
      userPrefs.startTime = Date().time
    }

    binding.viewAwaitingArirval.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(userTripsIntent(requireContext(), "trips_view", 0))
      userPrefs.startTime = Date().time
    }

    binding.viewInTransit.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(userTripsIntent(requireContext(), "trips_view", 1))
      userPrefs.startTime = Date().time
    }

    binding.viewAwaitingPod.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      analyticsUtil.trackEvent(
              EVENT_VIEW_TRIPS_AWAITING_POD,
              mutableListOf(PROPERTY_USER_ID , PROPERTY_TRIPS_AWAITING_POD_COUNT),
              mutableListOf(userPrefs.userId() , viewModel.awaitingPodCount)
      )
      action(NavigateHomeFragmentAction(PodFragment))
    }

    binding.viewAwaitingLoading.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(userTripsIntent(requireContext(), "trips_view", 2))
      userPrefs.startTime = Date().time
    }

    binding.viewAwaitingUnloading.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(userTripsIntent(requireContext(), "trips_view", 3))
      userPrefs.startTime = Date().time
    }

    binding.labelPaymentSummary.setOnClickListener {
      analyticsUtil.trackEvent(
              EVENT_VIEW_PAYMENT_SUMMARY,
              mutableListOf(PROPERTY_USER_ID),
              mutableListOf(userPrefs.userId())
      )
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(consolidatedPageIntent(requireContext()))
      }
    }

    binding.advanceCard.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(userTripsIntent(requireContext(), "payment_view", 0))
        userPrefs.startTime = Date().time
      }
    }

    binding.balanceCard.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(userTripsIntent(requireContext(), "payment_view", 1))
        userPrefs.startTime = Date().time
      }
    }

    binding.recoveryCard.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(userTripsIntent(requireContext(), "payment_view", 2))
        userPrefs.startTime = Date().time
      }
    }

    binding.editStickySearch.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(searchOngoingTripIntent(requireContext()))
      }
    }

    setText()
  }

  override fun onResume() {
    super.onResume()
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }
  }
  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showProgress()
          false -> uiUtils.hideProgress()
        }
      }
    }
  }

  private fun setText() {
    _title = "Ongoing Trips (${viewModel.totalOngoingCount})"
    binding.textAwaitingArrivalTripCount.text = viewModel.awaitingArrivalCount
    binding.textInTransitTripCount.text = viewModel.inTransitCount
    binding.textAwaitingPodTripCount.text = viewModel.awaitingPodCount
    binding.textAwaitingLoadingTripCount.text = viewModel.awaitingLoadingCount
    binding.textAwaitingUnloadingTripCount.text = viewModel.awaitingUnloadingCount
    binding.textAdvancePendingTripCount.text = viewModel.advancePendingCount
    binding.textAdvancePendingAmount.text = viewModel.advancePendingAmount
    binding.textBalancePendingTripCount.text = viewModel.balancePendingCount
    binding.textBalancePendingAmount.text = viewModel.balancePendingAmount
    binding.textRecoveryPendingTripCount.text = viewModel.recoveryPendingCount
    binding.textRecoveryPendingAmount.text = viewModel.recoveryPendingAmount
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_NO_TRIPS -> {
        if (resultCode == Activity.RESULT_OK) {
          action(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
      }
      else -> {

      }
    }
  }

  private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context,
      intent: Intent
    ) {
      val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
      if (downloadID == id) {
        uiUtils.showToast("File downloaded, please check notification.")
      }
    }
  }

  private fun downloadLedger(url: String) {

    val mgr = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val downloadUri = Uri.parse(url)
    val request = DownloadManager.Request(
        downloadUri
    )

    val filename = "Ledger.xlsx"
    val path = "/Axle App/$filename"
    request.setAllowedNetworkTypes(
        DownloadManager.Request.NETWORK_WIFI or
            DownloadManager.Request.NETWORK_MOBILE
    )
        .setTitle("Ledger Download")
        .setDescription("Downloading...")
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOCUMENTS,
            path
        )
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)


    downloadID = mgr.enqueue(request)

  }

  override fun onDestroy() {
    super.onDestroy()
    activity?.unregisterReceiver(onDownloadComplete)
  }

  private fun requestPermission() {
     compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {

          } else {
            uiUtils.showSnackbar(getString(string.storage_permission))
          }
        }
  }

}
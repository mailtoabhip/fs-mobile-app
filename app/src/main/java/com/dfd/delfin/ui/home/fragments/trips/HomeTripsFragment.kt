package com.dfd.delfin.ui.home.fragments.trips

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.databinding.FragmentHomeTripsBinding
import com.dfd.delfin.ui.bids.userTripsIntent
import com.dfd.delfin.ui.dialogs.DownloadLedgerDialog
import com.dfd.delfin.ui.dialogs.LedgerSuccessDialog
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment
import com.dfd.delfin.ui.home.fragments.HomeFragmentType.*
import com.dfd.delfin.ui.home.fragments.NavigateHomeFragmentAction
import com.dfd.delfin.ui.ledger.consolidatedPageIntent
import com.dfd.delfin.ui.searchongoingtrip.searchOngoingTripIntent
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.*
import javax.inject.Inject

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>()
{

  var _title: String = "My Trips"

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

  override fun getViewModelClass() = HomeTripsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_trips

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeTripsFragment_SetupTime")
    fragmentSetupTrace?.start()
    /**
     Track Event when trip_screen is created
     */
    analyticsUtil.moEngageTrackEvent(
            EVENT_VIEW_TRIPS,
            mutableListOf(PROPERTY_USER_ID),
            mutableListOf(userPrefs.userId())
    )
    viewModel.progressLiveData.reobserve(viewLifecycleOwner, ProgressObserver())

    viewModel.dataLoadingLiveData.observe(viewLifecycleOwner, Observer {
      userPrefs.awaitingArrivalCount=viewModel.awaitingArrivalCount
      uiUtils.hideProgress()
      setText()
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
      analyticsUtil.moEngageTrackEvent(
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
      analyticsUtil.moEngageTrackEvent(
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
  inner class ProgressObserver : Observer<Boolean?> {
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
    _title = "My Trips (${viewModel.totalOngoingCount})"
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


  /**
   * Download ledger Excel file using scoped storage (no permission needed)
   */
  private fun downloadLedger(url: String) {
    uiUtils.showProgress()
    val filename = "Ledger_${System.currentTimeMillis()}.xlsx"
    val storageDir = activity?.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
    val file = File(storageDir, filename)
    
    // Ensure directory exists
    storageDir?.mkdirs()
    
    compositeDisposable += io.reactivex.Observable.fromCallable {
      val client = OkHttpClient()
      val request = Request.Builder()
        .url(url)
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
      uiUtils.hideProgress()
      if (filePath != null) {
        uiUtils.showSnackbar("Ledger downloaded successfully: $filename")
      } else {
        uiUtils.showSnackbar("Download failed")
      }
    }, { error ->
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Download error: ${error.message}")
    })
  }

  override fun onDestroy() {
    super.onDestroy()
  }


}
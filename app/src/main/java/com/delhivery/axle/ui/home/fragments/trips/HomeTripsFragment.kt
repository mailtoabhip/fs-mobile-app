package com.delhivery.axle.ui.home.fragments.trips

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeTripsBinding
import com.delhivery.axle.ui.bids.userTripsIntent
import com.delhivery.axle.ui.dialogs.DownloadLedgerDialog
import com.delhivery.axle.ui.dialogs.LedgerSuccessDialog
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.PodFragment
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.searchongoingtrip.searchOngoingTripIntent
import com.delhivery.axle.utils.REQCODE_NO_TRIPS

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>()
{

  var _title: String = "Ongoing Trips"

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

    viewModel.fetchTripsSummary()

    viewModel.progressLiveData.reobserve(viewLifecycleOwner, ProgressObserver())

    viewModel.dataLoadingLiveData.observe(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      setText()
    })

    viewModel.downloadLoadingLiveData.observe(viewLifecycleOwner, Observer {
      uiUtils.showSnackbar("Ledger downloaded successfully")
    })

    viewModel.emailLoadingLiveData.observe(viewLifecycleOwner, Observer {
      LedgerSuccessDialog(context!!).show()
    })

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      uiUtils.showProgress()
      viewModel.fetchTripsSummary()
    }

    binding.downloadContainer.setOnClickListener {
      DownloadLedgerDialog(context!!, viewModel).show()
    }

    binding.labelViewAllTrips.setOnClickListener {
      startActivity(userTripsIntent(context!!, "all", 0))
    }

    binding.viewAwaitingArirval.setOnClickListener {
      startActivity(userTripsIntent(context!!, "trips_view", 0))
    }

    binding.viewInTransit.setOnClickListener {
      startActivity(userTripsIntent(context!!, "trips_view", 1))
    }

    binding.viewAwaitingPod.setOnClickListener {
      action(NavigateHomeFragmentAction(PodFragment))
    }

    binding.viewAwaitingLoading.setOnClickListener {
      startActivity(userTripsIntent(context!!, "trips_view", 2))
    }

    binding.viewAwaitingUnloading.setOnClickListener {
      startActivity(userTripsIntent(context!!, "trips_view", 3))
    }

    binding.labelPaymentSummary.setOnClickListener {
      context?.let {
        startActivity(consolidatedPageIntent(context!!))
      }
    }

    binding.advanceCard.setOnClickListener {
      context?.let {
        startActivity(userTripsIntent(context!!, "payment_view", 0))
      }
    }

    binding.balanceCard.setOnClickListener {
      context?.let {
        startActivity(userTripsIntent(context!!, "payment_view", 1))
      }
    }

    binding.recoveryCard.setOnClickListener {
      context?.let {
        startActivity(userTripsIntent(context!!, "payment_view", 2))
      }
    }

    binding.editStickySearch.setOnClickListener {
      context?.let {
        startActivity(searchOngoingTripIntent(context!!))
      }
    }

    setText()
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
          action(NavigateHomeFragmentAction(LoadsFragment))
        }
      }
      else -> {

      }
    }
  }

}
package com.delhivery.axle.ui.home.fragments.trips

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.trips.HomeTripsHeaderAction_AdvancePending
import com.delhivery.axle.data.home.trips.HomeTripsHeaderAction_BalancePending
import com.delhivery.axle.data.home.trips.HomeTripsHeaderAction_Completed
import com.delhivery.axle.data.home.trips.HomeTripsHeaderAction_InTransit
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutAction
import com.delhivery.axle.data.home.trips.HomeTripsWarningAction_NoLoads
import com.delhivery.axle.databinding.FragmentHomeTripsBinding
import com.delhivery.axle.repository.UserTripsLoadLimit
import com.delhivery.axle.ui.bids.TripType.AdvancePending
import com.delhivery.axle.ui.bids.TripType.BalancePending
import com.delhivery.axle.ui.bids.TripType.Completed
import com.delhivery.axle.ui.bids.TripType.InTransit
import com.delhivery.axle.ui.bids.userTripsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.EVENT_LIST_HEADER
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOCAL
import com.delhivery.axle.utils.PROPERTY_ITEM
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_NO_TRIPS
import com.delhivery.axle.utils.VALUE_ADVANCE_PENDING
import com.delhivery.axle.utils.VALUE_BALANCE_PENDING
import com.delhivery.axle.utils.VALUE_COMPLETED
import com.delhivery.axle.utils.VALUE_INTRANSIT
import com.delhivery.axle.utils.VALUE_TRIP

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>(),
    HomeTripsRVAdapterInterface, ToolbarElevationChangeListener {

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

  /* RV adapter */
  private val adapter: HomeTripsRVAdapter by lazy {
    HomeTripsRVAdapter(this)
  }

  override fun getViewModelClass() = HomeTripsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_trips

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      /* remove user trips and fetch again */
      refreshData()
    }

    /* setup recycler view */
    binding.rvTrips.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomeTripsFragment.adapter
      addOnScrollListener(HomeTripsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    /* observe and update adapter items */
    viewModel.userTripsData.reobserve(this, Observer {
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.tripsCountLiveData.reobserve(this, Observer {
      _title = when (it) {
        0, null -> getString(string.label_ongoing_trips)
        else -> "${getString(string.label_ongoing_trips)}($it)"
      }
    })

    viewModel.dataLoadingLiveData.reobserve(this, Observer {
      isLoadingData = it ?: false
    })

    /* attach sticky search with adapter */
    binding.editStickySearch.attachWithAdapter(adapter, this)

    /* fetch trips initially */
    fetchTripsData()
  }

  private fun fetchTripsData() {
    viewModel.fetchTripsSummary()
    viewModel.fetchTrips(false)
  }

  private fun refreshData() {
    adapter.resetStaticData()
    fetchTripsData()
  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsHeaderItem())
    add(1, HomeTripsSearchItem())
    add(2, HomeTripsProgressItem())
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        val _item = item.data as HomeTripsItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_TRIP, _item.transactionId)
        )
        context?.let {
          startActivity(
              tripDetailsIntent(_item, it)
          )
        }
      }

      HomeTripsSearchAction_Search -> context?.let {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_SEARCH_LOCAL,
            mutableListOf(PROPERTY_TRANSACTION_TYPE),
            mutableListOf(VALUE_TRIP)
        )

        val childView = binding.rvTrips.findViewHolderForAdapterPosition(1)!!.itemView
        val stickyView = binding.editStickySearch
        stickyView.visibility = View.VISIBLE
        stickyView.translationY = childView.top.toFloat()
        stickyView.alpha = 1f
        binding.rvTrips.alpha = 0f
        adapter.enableFilter()

        val valueAnimator = ValueAnimator.ofInt(childView.top, 0)
        valueAnimator.duration = 250
        valueAnimator.addUpdateListener { t ->
          val animValue = t.animatedValue as Int
          stickyView.translationY = animValue.toFloat()
          stickyView.setRatio((animValue.toFloat() / childView.top))
          if (animValue.toFloat() / childView.top == 1f) {
            binding.rvTrips.alpha = 1f
          }
        }
        valueAnimator.start()

        stickyView.postDelayed({
          stickyView.requestFocus()
          uiUtils.toggleKeyboard(false)
          toolbarElevationLiveData!!.postValue(0f)
        }, 300)
      }

      HomeTripsHeaderAction_AdvancePending -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_TRIP, VALUE_ADVANCE_PENDING)
        )
        startActivityForResult(userTripsIntent(context!!, AdvancePending), REQCODE_NO_TRIPS)
      }

      HomeTripsHeaderAction_BalancePending -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_TRIP, VALUE_BALANCE_PENDING)
        )
        startActivityForResult(userTripsIntent(context!!, BalancePending), REQCODE_NO_TRIPS)
      }

      HomeTripsHeaderAction_InTransit -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_TRIP, VALUE_INTRANSIT)
        )
        startActivityForResult(userTripsIntent(context!!, InTransit), REQCODE_NO_TRIPS)
      }

      HomeTripsHeaderAction_Completed -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_TRIP, VALUE_COMPLETED)
        )
        startActivityForResult(userTripsIntent(context!!, Completed), REQCODE_NO_TRIPS)
      }

      HomeTripsWarningAction_NoLoads -> {
        action(NavigateHomeFragmentAction(LoadsFragment))
      }

      HomeTripsTimeOutAction -> {
        refreshData()
      }
    }
  }

  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
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

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(UserTripsLoadLimit) {
    override fun loadMore() = viewModel.fetchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  inner class HomeTripsRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
    private val elevation: Float = 12f
  ) : OnScrollListener() {

    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (!adapter.checkFiltering()) {
        val layoutManager =
          (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
        val pos = layoutManager.findFirstVisibleItemPosition()
        val _toolbarElevation = if (pos == 0) {
          stickyView.translationY = 0f
          stickyView.visibility = View.GONE
          stickyView.alpha = 0f
          stickyView.setRatio(1f)
          defToolbarElevation
        } else if (pos == 1) {
          stickyView.visibility = View.VISIBLE
          val childView = recyclerView.findViewHolderForAdapterPosition(1)!!.itemView
          val viewTopGap = childView.height - stickyView.height * 1f
          val viewTop = childView.top + viewTopGap
          if (viewTop > 0) {
            val factor = viewTop / viewTopGap
            val invFactor = 1f - factor
            stickyView.translationY = viewTop
            stickyView.alpha = invFactor
            ViewCompat.setElevation(stickyView, elevation * invFactor)
          } else {
            stickyView.translationY = stickyView.top * 1f
            stickyView.alpha = 1f
            ViewCompat.setElevation(stickyView, elevation)
          }
          val factor =
            (childView.height.toFloat() - childView.bottom.toFloat()) / childView.height.toFloat()
          stickyView.setRatio((1 - factor))
          factor * defToolbarElevation
        } else {
          stickyView.visibility = View.VISIBLE
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          stickyView.setRatio(0f)
          0f
        }
        if (_toolbarElevation != toolbarElevation) {
          toolbarElevation = _toolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
      }
    }
  }
}
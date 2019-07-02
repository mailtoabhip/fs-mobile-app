package com.delhivery.orion.ui.home.fragments.trips

import android.animation.ValueAnimator
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_AdvancePending
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_BalancePending
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_Completed
import com.delhivery.orion.data.home.trips.HomeTripsHeaderAction_InTransit
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.orion.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.orion.data.home.trips.HomeTripsWarningAction_NoLoads
import com.delhivery.orion.databinding.FragmentHomeTripsBinding
import com.delhivery.orion.repository.UserTripsLoadLimit
import com.delhivery.orion.ui.bids.TripType.AdvancePending
import com.delhivery.orion.ui.bids.TripType.BalancePending
import com.delhivery.orion.ui.bids.TripType.Completed
import com.delhivery.orion.ui.bids.TripType.InTransit
import com.delhivery.orion.ui.bids.userTripsIntent
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.home.fragments.HomeFragmentType.BidsFragment
import com.delhivery.orion.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.orion.ui.tripdetails.tripDetailsIntent
import com.delhivery.orion.utils.PaginationScrollListener

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
      adapter.resetStaticData()
      fetchTripsData()
    }

    /* setup recycler view */
    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeTripsFragment.adapter
      addOnScrollListener(HomeTripsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    /* observe and update adapter items */
    viewModel.userTripsData.observe(this, Observer {
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
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
      HomeTripsRequestAction_ViewDetails -> context?.let {
        startActivity(
            tripDetailsIntent(item.data as HomeTripsItemData, it)
        )
      }
      HomeTripsSearchAction_Search -> context?.let {
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

        stickyView.postDelayed(Runnable {
          stickyView.requestFocus()
          uiUtils.toggleKeyboard(false)
          toolbarElevationLiveData!!.postValue(0f)
        }, 300)
      }
      HomeTripsHeaderAction_AdvancePending -> context?.let {
        startActivity(userTripsIntent(it, AdvancePending))
      }
      HomeTripsHeaderAction_BalancePending -> context?.let {
        startActivity(userTripsIntent(it, BalancePending))
      }
      HomeTripsHeaderAction_InTransit -> context?.let {
        startActivity(userTripsIntent(it, InTransit))
      }
      HomeTripsHeaderAction_Completed -> context?.let {
        startActivity(userTripsIntent(it, Completed))
      }
      HomeTripsWarningAction_NoLoads -> {
        action(NavigateHomeFragmentAction(BidsFragment))
      }
    }
  }

  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
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

      val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
      val pos = layoutManager.findFirstVisibleItemPosition()
      if (!adapter.checkFiltering()) {
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
package com.delhivery.orion.ui.home.fragments.trips

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.delhivery.orion.R
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.orion.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.orion.data.home.trips.TripStatus.InTrasit
import com.delhivery.orion.data.home.trips.TripStatus.TripCompleted
import com.delhivery.orion.data.home.trips.TripStatus.TruckArrived
import com.delhivery.orion.data.home.trips.TripStatus.TruckReached
import com.delhivery.orion.databinding.FragmentHomeTripsBinding
import com.delhivery.orion.repository.UserTripsLoadLimit
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.home.fragments.HomeFragmentType.BidsFragment
import com.delhivery.orion.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.orion.ui.tripdetails.tripDetailsIntent
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData
import com.delhivery.orion.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>(),
    HomeTripsRVAdapterInterface {

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

    binding.refreshLayout.progressLiveData(viewModel.progressLiveData, this)

    binding.refreshLayout.setOnRefreshListener {
      adapter.resetStaticData()
      /* remove user trips and fetch again */
      viewModel.fetchTrips(false)
    }

    /* setup recycler view */
    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeTripsFragment.adapter
      addOnScrollListener(HomeTripsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    /* no trips, start bidding button */
    binding.btnStartBidding.setOnClickListener { action(NavigateHomeFragmentAction(BidsFragment)) }

    /* observe and update adapter items */
    viewModel.userTripsData.observe(this, Observer { _items ->
      /* error container, if items are null */
      if (_items == null) {
        true
      } else {
        adapter.operation(_items)
        false
      }.let {
        binding.containerError.visible(it)
        binding.rvTrips.visible(!it)
      }
    })

    /* attach sticky search with adapter */
    binding.editStickySearch.attachWithAdapter(adapter)

    /* fetch trips initially */
    viewModel.fetchTrips(false)
  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsHeaderItem())
    add(1, HomeTripsProgressItem())
  }

  override fun onItemClicked(item: BaseHomeTripsRVAdapterItem<*>) {
    when (item.type) {
      TripItem -> {
        context?.let { startActivity(tripDetailsIntent(item.data as HomeTripsItemData, it)) }
      }
      else -> {/* useless */
      }
    }
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
        please(250, FastOutLinearInInterpolator()) {
          animate(binding.editStickySearch) {
            topOfItsParent(marginDp = 0f)
          }
        }.withEndAction {
          please(100, AccelerateDecelerateInterpolator()) {
            stickyView.requestFocus()
            stickyView.setRatio(0f)
            uiUtils.toggleKeyboard(false)
            binding.rvTrips.alpha = 1f
          }
        }
            .start()
      }
    }
  }

  /**10
   * Handle menu item clicked
   */
  private fun menuItemClicked(item: DelhiveryFabCardMenuItem) {
    when (item.id) {
      0 -> InTrasit
      1 -> TripCompleted
      2 -> TruckArrived
      3 -> TruckReached
      else -> null
    }.let {
      adapter.resetStaticData()
      viewModel.fetchTrips(false, it)
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(UserTripsLoadLimit) {
    override fun loadMore() = viewModel.fetchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = binding.refreshLayout.isRefreshing
  }

  inner class HomeTripsRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
    private val elevation: Float = 12f
  ) : OnScrollListener() {

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)

      val pos = layoutManager.findFirstVisibleItemPosition()
      if (!adapter.checkFiltering()) {
        if (pos == 0) {
          stickyView.translationY = 0f
          stickyView.visibility = View.GONE
          stickyView.alpha = 0f
          stickyView.setRatio(1f)
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
        } else {
          stickyView.visibility = View.VISIBLE
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          stickyView.setRatio(0f)
        }
      }
    }
  }
}
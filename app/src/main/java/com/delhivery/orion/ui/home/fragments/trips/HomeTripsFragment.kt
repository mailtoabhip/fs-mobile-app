package com.delhivery.orion.ui.home.fragments.trips

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.HomeTripsItemData
import com.delhivery.orion.data.home.TripStatus.InTrasit
import com.delhivery.orion.data.home.TripStatus.TripCompleted
import com.delhivery.orion.data.home.TripStatus.TruckArrived
import com.delhivery.orion.data.home.TripStatus.TruckReached
import com.delhivery.orion.databinding.FragmentHomeTripsBinding
import com.delhivery.orion.repository.UserTripsLoadLimit
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.home.fragments.HomeFragmentType.BidsFragment
import com.delhivery.orion.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.orion.ui.tripdetails.tripDetailsIntent
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData
import com.delhivery.orion.utils.extensions.visible

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>(),
    ItemClickListener<BaseHomeTripsRVAdapterItem<*>> {

  init {
//    toolbarElevationLiveData = MutableLiveData()
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
      adapter.reset()
      /* remove user trips and fetch again */
      viewModel.fetchTrips(false)
    }

    /* setup recycler view */
    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeTripsFragment.adapter
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    /* fab menu */
    binding.fabFilter.setOnClickListener { fab ->
      uiUtils.fabCardMenu(fab as FloatingActionButton, HomeTripsFabCardMenuItems) {
        menuItemClicked(it)
      }
    }

    /* no trips, start bidding button */
    binding.btnStartBidding.setOnClickListener { action(NavigateHomeFragmentAction(BidsFragment)) }

    /* observe and update adapter items */
    viewModel.userTripsData.observe(this, Observer { _items ->
      /* error container, if items are null */
      binding.containerError.visible(
          if (_items == null) {
            true
          } else {
            adapter.operation(_items)
            false
          }
      )
    })

    /* fetch trips initially */
    viewModel.fetchTrips(false)
  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsSearchItem())
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

  /**
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
      adapter.reset()
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
    private val stickyView: View,
    private val elevation: Float = 12f
  ) : OnScrollListener() {
    /* Current toolbar elevation */
    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)

      val pos = layoutManager.findFirstVisibleItemPosition()
      if (pos >= 1) {
        stickyView.alpha = 1f
//        please {
//          animate(stickyView) {
//            originalPosition()
//          }
//        }.start()
//        if (stickyView.translationY < 0) {
//        stickyView.animate()
//            .translationY(0f)
//            .start()
//        }
        toolbarElevationLiveData!!.postValue(0f)
      } else {
        val searchView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
        val factor =
          (searchView.height.toFloat() - searchView.bottom.toFloat()) / searchView.height.toFloat()
        stickyView.alpha = factor
//        stickyView.alpha = 0f
//        if (stickyView.translationY == 0f) {
//        stickyView.animate()
//            .translationY(-stickyView.height.toFloat())
//            .start()
//        please {
//          animate(stickyView) {
//            outOfScreen(Gravity.TOP)
//          }
//        }.start()
//        }
        toolbarElevationLiveData!!.postValue((1 - factor) * elevation)
      }
    }
  }
}
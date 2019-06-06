package com.delhivery.orion.ui.home.fragments.loads

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.loads.HomeLoadsSearchAction_Search
import com.delhivery.orion.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.orion.databinding.FragmentHomeLoadsBinding
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.searchload.SearchLoadActivity
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsFabCardMenuItems as HomeBidsFabCardMenuItems1

class HomeLoadsFragment : HomeBaseFragment<FragmentHomeLoadsBinding, HomeLoadsViewModel>(),
    HomeLoadsRVAdapterInterface {

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeLoadsFragment by lazy { HomeLoadsFragment() }
  }

  override fun getViewModelClass() = HomeLoadsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_loads

  /* RV adapter */
  private val adapter: HomeLoadsRVAdapter by lazy {
    HomeLoadsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshLayout.progressLiveData(viewModel.progressLiveData, this)

    binding.refreshLayout.setOnRefreshListener {
      adapter.resetStaticData()
      /* remove user transactions and fetch again */
      viewModel.fetchStaticData()
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeLoadsFragment.adapter
      addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    binding.editStickySearch.setOnClickListener {
      handleAction(
          HomeTripsSearchAction_Search, HomeLoadsSearchItem()
      )
    }

    //TODO: fix this after api
    /* observe and update adapter items */
//    viewModel.userBidsData.observe(this, Observer {
//      it?.let { _items -> adapter.operation(_items) }
//    })

    /* fetch static data and start fetching transactions */
//    viewModel.fetchStaticData()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onActivityCreated(savedInstanceState)
  }

  private fun getStaticItems() = mutableListOf<BaseHomeLoadsRVAdapterItem<*>>().apply {
    add(0, HomeLoadsSearchItem())
    add(1, HomeLoadsProgressItem())
    add(2, HomeLoadsProgressItem())
    add(3, HomeLoadsProgressItem())
    add(4, HomeLoadsProgressItem())
    add(5, HomeLoadsProgressItem())
    add(6, HomeLoadsProgressItem())
    add(7, HomeLoadsProgressItem())
    add(8, HomeLoadsProgressItem())
  }

  override fun onCreateOptionsMenu(
    menu: Menu?,
    inflater: MenuInflater?
  ) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_date_sort, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.nav_date -> {
        return true
      }
      else ->
        return super.onOptionsItemSelected(item)
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> context?.let {
        startActivity(
            bidDetailsIntent(item.data as HomeBidsRequestItemData, it)
        )
      }
      HomeLoadsSearchAction_Search -> context?.let {
        startActivity(
            Intent(it, SearchLoadActivity::class.java)
        )
      }
    }
  }

  /**
   * Home bids rv scroll listener for search bar animation related stuff
   */
  inner class HomeBidsRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
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
      val _toolbarElevation = if (pos == 0) {
        val childView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
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

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchUserTransactions(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = binding.refreshLayout.isRefreshing
  }
}
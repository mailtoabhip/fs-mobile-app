package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.HomeBidsHeaderAction_ConfirmedBids
import com.delhivery.orion.data.home.HomeBidsHeaderAction_MyBids
import com.delhivery.orion.data.home.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.HomeBidsSearchAction_Search
import com.delhivery.orion.data.home.HomeBidsWarningAction_EditRoutePrefs
import com.delhivery.orion.data.home.HomeBidsWarningAction_SelectRoutes
import com.delhivery.orion.databinding.FragmentHomeBidsBinding
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.bids.BidType.ActiveBid
import com.delhivery.orion.ui.bids.BidType.ConfirmedBid
import com.delhivery.orion.ui.bids.userBidsIntent
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.searchload.SearchLoadActivity
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.UserRoutes
import com.delhivery.orion.ui.selectroute.selectRouteIntent
import com.delhivery.orion.utils.PaginationScrollListener

class HomeBidsFragment : HomeBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    HomeBidsRVAdapterInterface {

  init {
    toolbarElevationLiveData = MutableLiveData()
  }

  companion object {
    /* singleton instance */
    val _instance: HomeBidsFragment by lazy { HomeBidsFragment() }
  }

  override fun getViewModelClass() = HomeBidsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_bids

  /* RV adapter */
  private val adapter: HomeBidsRVAdapter by lazy {
    HomeBidsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.progressLiveData.observe(this, Observer {
      it?.let { show ->
        if (!show) {
          binding.refreshLayout.isRefreshing = false
        } else if (!binding.refreshLayout.isRefreshing) {
          binding.refreshLayout.isRefreshing = true
        }
      }
    })

    binding.refreshLayout.setOnRefreshListener {
      adapter.removeAllTransactions()
      /* remove user transactions and fetch again */
      viewModel.fetchUserTransactions(false)
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticData())

    /* Use this logic to create our own menu as per  */
    binding.fabSort.setOnClickListener { fab ->
      uiUtils.fabCardMenu(fab as FloatingActionButton, HomeBidsFabCardMenuItems) {
        onFabMenuItemSelected(it)
      }
    }

    /* start search on click */
    binding.editStickySearch.setOnClickListener {
      handleAction(
          HomeBidsSearchAction_Search, HomeBidsSearchItem()
      )
    }

    /* observe and update adapter for transactions */
    viewModel.transactionData.observe(this, Observer {
      mutableListOf<Pair<BaseHomeBidsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
        it?.forEachIndexed { i, _item ->
          add(i, Pair(HomeBidsRequestItem(_item), Add))
        }
      }
          .let { items ->
            adapter.operation(items.toList())
          }
    })

    viewModel.fetchUserTransactions(false)
  }

  private fun getStaticData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem())
//    add(
//        2, HomeBidsWarningItem(
//        HomeBidsWarningItemData(
//            "No Routes selected", "Please select your route preference to see the load requests",
//            "Select routes", HomeBidsWarningAction_SelectRoutes
//        )
//    )
//    )
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsHeaderAction_MyBids -> context?.let { startActivity(userBidsIntent(it, ActiveBid)) }
      HomeBidsHeaderAction_ConfirmedBids -> context?.let {
        startActivity(userBidsIntent(it, ConfirmedBid))
      }
      HomeBidsWarningAction_EditRoutePrefs, HomeBidsWarningAction_SelectRoutes -> context?.let {
        startActivity(selectRouteIntent(it, UserRoutes))
      }
      HomeBidsRequestAction_ViewDetails -> context?.let { startActivity(bidDetailsIntent(it)) }
      HomeBidsSearchAction_Search -> context?.let {
        startActivity(
            Intent(it, SearchLoadActivity::class.java)
        )
      }
    }
  }

  private fun onFabMenuItemSelected(item: DelhiveryFabCardMenuItem) {
    /* todo - handle sorting here */
  }

  /**
   * Home bids rv scroll listener for search bar animation related stuff
   */
  inner class HomeBidsRVScrollListener(
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
      val viewVisibility = if (pos >= 1) {
        val _toolbarElevation = if (pos == 1) {
          val childView = recyclerView.findViewHolderForAdapterPosition(1)!!.itemView

          val viewTopGap = childView.height - stickyView.height * 1f
          val viewTop = childView.top + viewTopGap
          if (viewTop > 0) {
            val factor = viewTop / viewTopGap
            val invFactor = 1f - factor
            stickyView.translationY = viewTop
            stickyView.alpha = invFactor
            ViewCompat.setElevation(stickyView, elevation * invFactor)
            factor * defToolbarElevation
          } else {
            stickyView.translationY = stickyView.top * 1f
            stickyView.alpha = 1f
            ViewCompat.setElevation(stickyView, elevation)
            0f
          }
        } else {
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          0f
        }
        if (_toolbarElevation != toolbarElevation) {
          toolbarElevation = _toolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
        View.VISIBLE
      } else {
        if (toolbarElevation != defToolbarElevation) {
          toolbarElevation = defToolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
        View.GONE
      }
      if (stickyView.visibility != viewVisibility) {
        if (stickyView.visibility == View.GONE) {
          binding.fabSort.hide()
        } else {
          binding.fabSort.show()
        }
        uiUtils.toggleKeyboard()
        stickyView.visibility = viewVisibility
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
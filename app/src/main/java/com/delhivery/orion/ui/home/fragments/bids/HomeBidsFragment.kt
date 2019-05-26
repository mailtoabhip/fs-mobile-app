package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_ConfirmedBids
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_LostBids
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_MyBids
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_EditRoutePrefs
import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_SelectRoutes
import com.delhivery.orion.databinding.FragmentHomeBidsBinding
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.bids.BidType.ActiveBid
import com.delhivery.orion.ui.bids.BidType.ConfirmedBid
import com.delhivery.orion.ui.bids.BidType.LostBid
import com.delhivery.orion.ui.bids.userBidsIntent
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.UserRoutes
import com.delhivery.orion.ui.selectroute.selectRouteIntent
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData

class HomeBidsFragment : HomeBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    HomeBidsRVAdapterInterface {

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
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

    binding.refreshLayout.progressLiveData(viewModel.progressLiveData, this)

    binding.refreshLayout.setOnRefreshListener {
      adapter.resetStaticData()
      /* remove user transactions and fetch again */
      viewModel.fetchStaticData()
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticData())

    /* observe and update adapter items */
    viewModel.userBidsData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    /* fetch static data and start fetching transactions */
    viewModel.fetchStaticData()
  }

  private fun getStaticData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsProgressItem())
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
      HomeBidsHeaderAction_LostBids -> context?.let {
        startActivity(userBidsIntent(it, LostBid))
      }
      HomeBidsWarningAction_EditRoutePrefs, HomeBidsWarningAction_SelectRoutes -> context?.let {
        startActivity(selectRouteIntent(it, UserRoutes))
      }
      HomeBidsRequestAction_ViewDetails -> context?.let {
        startActivity(
            bidDetailsIntent(item.data as HomeBidsRequestItemData, it)
        )
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
package com.delhivery.axle.ui.home.fragments.bids

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsHeaderAction_ConfirmedBids
import com.delhivery.axle.data.home.bids.HomeBidsHeaderAction_LostBids
import com.delhivery.axle.data.home.bids.HomeBidsHeaderAction_MyBids
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.HomeBidsSearchAction_Search
import com.delhivery.axle.data.home.bids.HomeBidsSearchItemData
import com.delhivery.axle.data.home.bids.HomeBidsTimeOutAction
import com.delhivery.axle.data.home.bids.HomeBidsWarningAction_EditRoutePrefs
import com.delhivery.axle.data.home.bids.HomeBidsWarningAction_SelectRoutes
import com.delhivery.axle.databinding.FragmentHomeBidsBinding
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.bids.BidType.ActiveBid
import com.delhivery.axle.ui.bids.BidType.ConfirmedBid
import com.delhivery.axle.ui.bids.BidType.LostBid
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.EVENT_LIST_HEADER
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOCAL
import com.delhivery.axle.utils.PROPERTY_ITEM
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.VALUE_ACTIVE
import com.delhivery.axle.utils.VALUE_BID
import com.delhivery.axle.utils.VALUE_CONFIRMED
import com.delhivery.axle.utils.VALUE_LOST

class HomeBidsFragment : HomeBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    HomeBidsRVAdapterInterface, ToolbarElevationChangeListener {

  var _title: String = "My Bids"

  override val title: CharSequence
    get() = _title

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

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      /* remove user transactions and fetch again */
      refreshData()
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticData())

    /* observe and update adapter items */
    viewModel.userBidsData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.bidsCountLiveData.observe(this, Observer {
      _title = when (it) {
        0, null -> "My Bids"
        else -> "My Bids(" + it + ")"
      }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    /* attach sticky search with adapter */
    binding.editStickySearch.attachWithAdapter(adapter, this)

    /* fetch bids data*/
    fetchBidsData()
  }

  private fun fetchBidsData() {
    viewModel.fetchBidsSummary()
    viewModel.fetchBids()
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData()
    /* fetch again */
    fetchBidsData()
  }

  private fun getStaticData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem(HomeBidsSearchItemData()))
    add(2, HomeBidsProgressItem())
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsHeaderAction_MyBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_BID, VALUE_ACTIVE)
        )
        context?.let { startActivity(userBidsIntent(it, ActiveBid)) }
      }

      HomeBidsHeaderAction_ConfirmedBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_BID, VALUE_CONFIRMED)
        )
        context?.let {
          startActivity(userBidsIntent(it, ConfirmedBid))
        }
      }

      HomeBidsHeaderAction_LostBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_HEADER,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_ITEM),
            mutableListOf(VALUE_BID, VALUE_LOST)
        )
        context?.let {
          startActivity(userBidsIntent(it, LostBid))
        }
      }

      HomeBidsWarningAction_EditRoutePrefs, HomeBidsWarningAction_SelectRoutes -> context?.let {
        startActivity(selectRouteIntent(it, EditRoute))
      }

      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )

        context?.let {
          startActivity(bidDetailsIntent(_item, it))
        }
      }

      HomeBidsSearchAction_Search -> context?.let {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_SEARCH_LOCAL,
            mutableListOf(PROPERTY_TRANSACTION_TYPE),
            mutableListOf(VALUE_BID)
        )

        val childView = binding.rvBids.findViewHolderForAdapterPosition(1)!!.itemView
        val stickyView = binding.editStickySearch
        stickyView.visibility = View.VISIBLE
        stickyView.translationY = childView.top.toFloat()
        stickyView.alpha = 1f
        binding.rvBids.alpha = 0f
        adapter.enableFilter()

        val valueAnimator = ValueAnimator.ofInt(childView.top, 0)
        valueAnimator.duration = 250
        valueAnimator.addUpdateListener { t ->
          val animValue = t.animatedValue as Int
          stickyView.translationY = animValue.toFloat()
          stickyView.setRatio((animValue.toFloat() / childView.top))
          if (animValue.toFloat() / childView.top == 1f) {
            binding.rvBids.alpha = 1f
          }
        }
        valueAnimator.start()
        stickyView.postDelayed(Runnable {
          stickyView.requestFocus()
          uiUtils.toggleKeyboard(false)
          toolbarElevationLiveData!!.postValue(0f)
        }, 300)
      }

      HomeBidsTimeOutAction -> {
        refreshData()
      }
    }
  }

  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchBids(true)

    override fun hasMore() = viewModel.offset < viewModel.total

    override fun isLoading() = isLoadingData
  }

  inner class HomeBidsRVScrollListener(
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

      val layoutManager =
        (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
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
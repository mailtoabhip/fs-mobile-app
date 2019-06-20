package com.delhivery.orion.ui.home.fragments.bids

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
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_ConfirmedBids
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_LostBids
import com.delhivery.orion.data.home.bids.HomeBidsHeaderAction_MyBids
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.bids.HomeBidsSearchAction_Search
import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_EditRoutePrefs
import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_SelectRoutes
import com.delhivery.orion.databinding.FragmentHomeBidsBinding
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.bids.BidType.ActiveBid
import com.delhivery.orion.ui.bids.BidType.ConfirmedBid
import com.delhivery.orion.ui.bids.BidType.LostBid
import com.delhivery.orion.ui.bids.userBidsIntent
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.orion.ui.selectroute.activity.selectRouteIntent
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData

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

    binding.refreshLayout.progressLiveData(viewModel.progressLiveData, this)

    binding.refreshLayout.setOnRefreshListener {
      adapter.resetStaticData()
      /* remove user transactions and fetch again */
      viewModel.fetchBids()
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
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
        null -> "My Bids"
        0 -> "My Bids"
        else -> "My Bids(" + it + ")"
      }
    })

    /* attach sticky search with adapter */
    binding.editStickySearch.attachWithAdapter(adapter, this)

    /* fetch static data and start fetching transactions */
    viewModel.fetchBids()
  }

  private fun getStaticData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem())
    add(2, HomeBidsProgressItem())
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
        startActivity(selectRouteIntent(it, EditRoute))
      }
      HomeBidsRequestAction_ViewDetails -> context?.let {
        startActivity(bidDetailsIntent(item.data as HomeBidsRequestItemData, it))
      }
      HomeBidsSearchAction_Search -> context?.let {
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

    override fun isLoading() = binding.refreshLayout.isRefreshing
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
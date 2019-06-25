package com.delhivery.orion.ui.home.fragments.loads

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.delhivery.orion.R
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_Accept
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.orion.data.home.loads.HomeLoadsInfoAction_Search
import com.delhivery.orion.data.home.loads.HomeLoadsSearchAction_Search
import com.delhivery.orion.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.orion.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.orion.databinding.FragmentHomeLoadsBinding
import com.delhivery.orion.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.orion.ui.home.TitleProvider
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment
import com.delhivery.orion.ui.searchload.SearchLoadActivity
import com.delhivery.orion.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.orion.ui.selectroute.activity.selectRouteIntent
import com.delhivery.orion.utils.PaginationScrollListener
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation

class HomeLoadsFragment : HomeBaseFragment<FragmentHomeLoadsBinding, HomeLoadsViewModel>(),
    HomeLoadsRVAdapterInterface, TitleProvider {

  var _title: String = "Load Request"

  override val title: CharSequence
    get() = _title

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = false

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

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      adapter.resetStaticData()
      /* remove user transactions and fetch again */
      viewModel.fetchUserTransactions()
    }

    /* setup recycler view */
    binding.rvLoads.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeLoadsFragment.adapter
      addOnScrollListener(HomeLoadsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    binding.editStickySearch.setOnClickListener {
      handleAction(
          HomeTripsSearchAction_Search, HomeLoadsSearchItem()
      )
    }

    binding.routesBanner.setOnClickListener {
      startActivity(
          selectRouteIntent(it.context, EditRoute)
      )
    }

    viewModel.userLoadsData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.loadsCountLiveData.observeOnce(this, Observer {
      _title = when (it) {
        null -> "Load Request"
        0 -> "Load Request"
        else -> "Load Request(" + it + ")"
      }
      this@HomeLoadsFragment.activity?.title = _title
    })

    viewModel.routesLiveData.observe(this, Observer {
      when (it) {
        false -> binding.rvLoads.apply {
          this@HomeLoadsFragment.visible = true
          binding.routesBanner.visibility = View.VISIBLE
          addOnScrollListener(BannerRVScrollListener())
        }

        true -> {
          binding.routesBanner.visibility = View.GONE
          this@HomeLoadsFragment.visible = false
        }
      }
    })

    viewModel.bidsStatusLiveData.observe(this, Observer {
      when {
        it != -1 && it != null -> {
          (adapter.itemsList()
              .get(it).data as HomeBidsRequestItemData).showing = true
          adapter.notifyItemChanged(it)
        }
      }
    })

    /* fetch user transactions */
    viewModel.fetchUserTransactions()
  }

  private fun getStaticItems() = mutableListOf<BaseHomeLoadsRVAdapterItem<*>>().apply {
    add(0, HomeLoadsSearchItem())
    add(1, HomeLoadsProgressItem())
  }

  override fun onResume() {
    super.onResume()
    /* check user route/lane preferences*/
    viewModel.checkUserRoutes()
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
      HomeLoadsInfoAction_Search, HomeLoadsSearchAction_Search -> context?.let {
        startActivity(
            Intent(it, SearchLoadActivity::class.java)
        )
      }
      HomeLoadsInfoAction_EditRoute, HomeLoadsWarningAction_NoLoads -> context?.let {
        startActivity(selectRouteIntent(it, EditRoute))
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>,
    position: Int
  ) {
    when (actionId) {
      HomeBidsRequestAction_PlaceBid -> {
        (item.data as HomeBidsRequestItemData).let {
          BidDetailsCreateEditDialog(context!!, it, null, viewModel, position).show()
        }
      }
      HomeBidsRequestAction_Accept -> {
        (item.data as HomeBidsRequestItemData).let {
          BidDetailsCreateEditDialog(context!!, it, null, viewModel, position).show()
        }
      }
    }
  }

  fun hide() {
    binding.routesBanner.animate()
        .translationY(
            PositionAnimExpectation.dpToPx(
                this@HomeLoadsFragment.context!!, binding.routesBanner.height.toFloat()
            )
        )
        .setInterpolator(AccelerateInterpolator(2f))
        .setDuration(200L)
        .start();
  }

  fun show() {
    binding.routesBanner.animate()
        .translationY(
            -PositionAnimExpectation.dpToPx(
                this@HomeLoadsFragment.context!!, 0f
            )
        )
        .setInterpolator(DecelerateInterpolator(2f))
        .setDuration(400L)
        .start()
  }

  /**
   * Home loads rv scroll listener for search bar animation related stuff
   */
  inner class HomeLoadsRVScrollListener(
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
        defToolbarElevation
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
   * Home loads rv scroll listener for banner animation related stuff
   */
  inner class BannerRVScrollListener() : OnScrollListener() {

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide();
        scrollDist = 0;
        visible = false;
      } else if (!visible && scrollDist < -MINIMUM) {
        show();
        scrollDist = 0;
        visible = true;
      }

      if ((visible && dy > 0) || (!visible && dy < 0)) {
        scrollDist += dy;
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
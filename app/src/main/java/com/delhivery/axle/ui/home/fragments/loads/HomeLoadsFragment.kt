package com.delhivery.axle.ui.home.fragments.loads

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_EditRoute
import com.delhivery.axle.data.home.loads.HomeLoadsInfoAction_Search
import com.delhivery.axle.data.home.loads.HomeLoadsSearchAction_Search
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutAction
import com.delhivery.axle.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.databinding.FragmentHomeLoadsBinding
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.Config
import com.delhivery.axle.utils.ContactUtils
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_EDIT_ROUTE
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.VALUE_LOAD
import com.delhivery.axle.utils.VALUE_LOAD_INFO
import com.delhivery.axle.utils.VALUE_NO_RESULTS
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation
import javax.inject.Inject

class HomeLoadsFragment : HomeBaseFragment<FragmentHomeLoadsBinding, HomeLoadsViewModel>(),
    HomeLoadsRVAdapterInterface, TitleProvider {

  var _title: String = "Load Request"

  override val title: CharSequence
    get() = _title

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = false

  @Inject lateinit var dialogUtils: DialogUtils

  @Inject lateinit var contactUtils: ContactUtils

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
      refreshData()
    }

    /* setup recycler view */
    binding.rvLoads.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
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

    viewModel.progressLiveData.observe(this, ProgressObserver())

    viewModel.userLoadsData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.loadsCountLiveData.observe(this, Observer {
      _title = when (it) {
        0, null -> "Load Request"
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

    viewModel.bidsActionLiveData.observe(this, Observer {
      uiUtils.toggleKeyboard()
          .apply {
            when {
              it != null -> {
                (adapter.itemsList()
                    .get(it.first).data as HomeBidsRequestItemData).transactionBid = it.second
                adapter.notifyItemChanged(it.first)
              }
            }
          }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    /* fetch user transactions */
    if (!viewModel.isRouteUpdated()) {
      viewModel.fetchUserTransactions()
    }

    viewModel.updateUserAppAccess()
  }

  override fun onResume() {
    super.onResume()
    /* check user route/lane preferences*/
    viewModel.checkUserRoutes()
    /* fetch new loads is routes updated*/
    if (viewModel.isRouteUpdated()) {
      refreshData()
      viewModel.setRouteUpdated()
    }
  }

  private fun getStaticItems() = mutableListOf<BaseHomeLoadsRVAdapterItem<*>>().apply {
    add(0, HomeLoadsSearchItem())
    add(1, HomeLoadsProgressItem())
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData()
    /* fetch again */
    viewModel.fetchUserTransactions()
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_LOAD, _item.transactionId ?: "")
        )
        context?.let { startActivity(bidDetailsIntent(_item, it)) }
      }

      HomeLoadsInfoAction_Search, HomeLoadsSearchAction_Search -> {
        context?.let {
          startActivity(
              Intent(it, SearchLoadActivity::class.java)
          )
        }
      }

      HomeLoadsInfoAction_EditRoute -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_EDIT_ROUTE,
            mutableListOf(PROPERTY_SOURCE),
            mutableListOf(VALUE_LOAD_INFO)
        )
        context?.let {
          startActivity(selectRouteIntent(it, EditRoute))
        }
      }

      HomeLoadsWarningAction_NoLoads -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_EDIT_ROUTE,
            mutableListOf(PROPERTY_SOURCE),
            mutableListOf(VALUE_NO_RESULTS)
        )
        context?.let {
          startActivity(selectRouteIntent(it, EditRoute))
        }
      }

      HomeLoadsTimeOutAction -> {
        refreshData()
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>,
    position: Int
  ) {
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        when (actionId) {
          HomeBidsRequestAction_PlaceBid -> {
            (item.data as HomeBidsRequestItemData).let {
              BidDetailsCreateEditDialog(
                  context!!, it, it.transactionBid, viewModel, position, analyticsUtil
              ).show()
            }
          }
        }
      }
      UNAPPROVED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_not_approved,
            string.msg_dialog_supplier_not_approved,
            "EXIT", "MAIL US",
            {
              it.dismiss()
            },
            {
              when (contactUtils.openGmail(receiver = Config.AxleSupportEmail)) {
                false -> {
                  it.dismiss()
                  uiUtils.showToast("Sorry...You don't have any mail app installed")
                }
                else -> {
                }
              }
            }
        )
      }
      DISABLED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_disabled,
            string.msg_dialog_supplier_disabled,
            "EXIT", "MAIL US",
            {
              it.dismiss()
            },
            {
              when (contactUtils.openGmail(receiver = Config.AxleSupportEmail)) {
                false -> {
                  it.dismiss()
                  uiUtils.showToast("Sorry...You don't have any mail app installed")
                }
                else -> {
                }
              }
            }
        )
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
        .start()
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
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showProgress("Placing your bid, hang on!")
          false -> uiUtils.hideProgress()
        }
      }
    }
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
      recyclerView: androidx.recyclerview.widget.RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager =
        (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
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
  inner class BannerRVScrollListener : OnScrollListener() {

    override fun onScrolled(
      recyclerView: androidx.recyclerview.widget.RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide()
        scrollDist = 0
        visible = false
      } else if (!visible && scrollDist < -MINIMUM) {
        show()
        scrollDist = 0
        visible = true
      }

      if ((visible && dy > 0) || (!visible && dy < 0)) {
        scrollDist += dy
      }
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchUserTransactions(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }
}
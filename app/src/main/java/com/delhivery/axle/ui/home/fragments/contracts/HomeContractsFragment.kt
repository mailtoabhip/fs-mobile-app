package com.delhivery.axle.ui.home.fragments.contracts

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterExpress
import com.delhivery.axle.data.home.contracts.HomeContractsFilterInfo
import com.delhivery.axle.data.home.contracts.HomeContractsFilterNonExpress
import com.delhivery.axle.data.home.loads.HomeLoadsAddTruckItemDataConfig
import com.delhivery.axle.data.home.loads.HomeLoadsFilterAction
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutAction
import com.delhivery.axle.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.axle.databinding.DialogContractsTypeInfoBinding
import com.delhivery.axle.databinding.DialogKycSubmittedBinding
import com.delhivery.axle.databinding.FragmentHomeContractsBinding
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.activity.home.orderRank
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.UpdateTabCountAndBadgeInterface
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_EDIT_ROUTE
import com.delhivery.axle.utils.EVENT_FILTER_EXPRESS_LOADS
import com.delhivery.axle.utils.EVENT_FILTER_VEHICLE_TYPE
import com.delhivery.axle.utils.EVENT_HOME_ORDER_CARD_CLICK
import com.delhivery.axle.utils.EVENT_HOME_SEARCH_INITIATE
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_LOADFEED_BID_INITIATE
import com.delhivery.axle.utils.EVENT_LOADFEED_BID_REVISE_INITIATED
import com.delhivery.axle.utils.EVENT_LOADFEED_BID_REVISE_SUBMITTED
import com.delhivery.axle.utils.EVENT_LOADFEED_BID_SUBMIT
import com.delhivery.axle.utils.EVENT_LOAD_SCROLL
import com.delhivery.axle.utils.EVENT_SHOW_ADDITIONAL_LOADS
import com.delhivery.axle.utils.FCMUtils
import com.delhivery.axle.utils.PROPERTY_BID_COUNT
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_NO_OF_SCROLLS
import com.delhivery.axle.utils.PROPERTY_ORDER_COUNT
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_ORDER_LOWEST_BID_VALUE
import com.delhivery.axle.utils.PROPERTY_ORDER_RANK
import com.delhivery.axle.utils.PROPERTY_OVERALL_PERFORMANCE
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE_NEW
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE_OLD
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PROPERTY_VEHICLE_REPORTING_DATE_TIME
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_ADD_TRUCK
import com.delhivery.axle.utils.REQCODE_EDIT_ROUTE
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_SCROLL_BANNER
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_TOP_BANNER
import com.delhivery.axle.utils.VALUE_BANNER
import com.delhivery.axle.utils.VALUE_LOAD
import com.delhivery.axle.utils.VALUE_NO_RESULTS
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeContractsFragment :HomeLoadsTruckBaseFragment<FragmentHomeContractsBinding,HomeContractsViewModel>(),
  HomeContractsRVAdapterInterface, TitleProvider {

  var _title: String = "Contracts"

  override val title: CharSequence
    get() = _title

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var fcmUtils: FCMUtils
  @Inject lateinit var userPrefs: UserPrefs

  var visible = false
  var demandType: String = ""
  var isInternal = false
  var pos = 0



  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeContractsFragment by lazy { HomeContractsFragment() }
  }

  override fun getViewModelClass() = HomeContractsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_contracts

  /* RV adapter */
  private val adapter: HomeContractsRVAdapter by lazy {
    HomeContractsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    demandType = userPrefs.demandType

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    /* setup recycler view */
    binding.rvLoads.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeContractsFragment.adapter
      addOnScrollListener(PaginationInterface())
    }

    binding.rvLoads.setItemAnimator(null);


    viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.loadsCountLiveData.reobserve(viewLifecycleOwner, Observer {
      HomeLoadsTruckFragment._instance.dataToUpdate("contracts",it>0,it)
      _title = when (it) {
        0, null -> getString(string.label_load_request)
        else -> "${getString(string.label_load_request)}($it)"
      }
    })


    refreshData()
  }



  override fun onResume() {
    super.onResume()
    viewModel.paginateCount = 0
    if (viewModel.fromNotification) {
      refreshData()
      viewModel.fromNotification = false
    }
  }

  override fun onStop() {
    super.onStop()
    if (viewModel.paginateCount > 0) {
      analyticsUtil.trackEvent(
        EVENT_LOAD_SCROLL,
        mutableListOf(PROPERTY_USER_ID, PROPERTY_DEMAND_TYPE, PROPERTY_NO_OF_SCROLLS, PROPERTY_OVERALL_PERFORMANCE),
        mutableListOf(userPrefs.userId(), userPrefs.demandType, viewModel.paginateCount.toString(), userPrefs.userPerformance)
      )
    }
    viewModel.paginateCount = 0
  }

  override fun onPause() {
    super.onPause()
    viewModel.paginateCount = 0
  }

  private fun refreshData() {
    viewModel.paginateCount = 0
    viewModel.hasOrionLoadOnce = false
    adapter.resetStaticData()
    viewModel.fetchUserTransactions(false, demandType, isInternal)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val data = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.moEngageTrackEvent(
          EVENT_HOME_ORDER_CARD_CLICK,
          mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
          mutableListOf(
            data.transactionId ?: " ",
            (orderRank - STATIC_ITEM_LIST - ((orderRank - STATIC_ITEM_LIST).div(
              HomeLoadsAddTruckItemDataConfig
            ))).toString(),
            viewModel.total.toString()
          )
        )
        analyticsUtil.trackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_LOAD, data.transactionId ?: "")
        )
        context?.let {
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(contractDetailsIntent(data.key(), it))
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
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(userRoutesIntent(it))
        }
      }

      HomeLoadsTimeOutAction -> {
        refreshData()
      }
      HomeContractsFilterExpress -> {
        isInternal = true
        demandType = "Internal"
        refreshData()
      }
      HomeContractsFilterNonExpress -> {
        isInternal = false
        demandType = "Corporate"
        refreshData()
      }
      HomeContractsFilterInfo -> {
        infoDialog()
      }

      HomeLoadsFilterAction -> {
        //Capture Event
        analyticsUtil.trackEvent(
          EVENT_FILTER_EXPRESS_LOADS,
          mutableListOf(PROPERTY_USER_ID),
          mutableListOf(userPrefs.userId())
        )


        demandType = userPrefs.demandType
        isInternal = demandType =="Internal"


        refreshData()
      }

    }
  }

  private fun infoDialog() {
    if(activity!=null) {
      val dialog = Dialog(activity!!)
      val bindingDialog = DialogContractsTypeInfoBinding.inflate(activity!!.layoutInflater)
      bindingDialog.buttonCancel.setOnClickListener {
        dialog.cancel()
      }
      bindingDialog.rule1.text =   HtmlCompat.fromHtml(getString(R.string.express_load_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      bindingDialog.rule2.text =   HtmlCompat.fromHtml(getString(R.string.non_express_load_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setContentView(bindingDialog.root)
      dialog.show()
      dialog.window!!.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>,
    position: Int
  ) {
  }

  /**
   * Home contracts rv scroll listener for search bar animation related stuff
   */
  inner class HomeContractsRVScrollListener(
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

      val layoutManager: LinearLayoutManager? = recyclerView.layoutManager as? LinearLayoutManager
      val pos = layoutManager?.findFirstVisibleItemPosition()
      val toolbarElevation = if (pos == 0) {
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
      if (toolbarElevation != this.toolbarElevation && toolbarElevationLiveData != null) {
        this.toolbarElevation = toolbarElevation
        toolbarElevationLiveData?.postValue(this.toolbarElevation)
      }
    }
  }


  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(50) {
    override fun loadMore() = viewModel.fetchUserTransactions(true, demandType, isInternal)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

}
const val STATIC_ITEM_LIST = 3
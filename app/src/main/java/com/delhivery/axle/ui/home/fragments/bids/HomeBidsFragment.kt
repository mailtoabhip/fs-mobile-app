package com.delhivery.axle.ui.home.fragments.bids

import android.animation.ValueAnimator
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.databinding.FragmentHomeBidsBinding
import com.delhivery.axle.ui.biddetails.BidDetailsActivity
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.BulkBidDetailsDialog
import com.delhivery.axle.ui.contractDetails.ContractDetailsActivity
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.custom.DelhiveryBidAnimatedSearchBar
import com.delhivery.axle.ui.home.activity.home.OFF_SET_LIMIT
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.Date
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * All bids screen on home
 */
class HomeBidsFragment : HomeLoadsTruckBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    HomeBidsRVAdapterInterface, DelhiveryBidAnimatedSearchBar.ToolbarElevationChangeListener {

  var _title: String = "My Bids"
  var launch : Boolean =true
  @Inject lateinit var userPrefs: UserPrefs

  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true

  companion object {
    /* singleton instance */
    val _instance: HomeBidsFragment by lazy { HomeBidsFragment() }
  }

  override fun getViewModelClass() = HomeBidsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_bids

  /* RV adapter */
  private val adapter: HomeBidsRVAdapter by lazy { HomeBidsRVAdapter(this) }
  var limit = OFF_SET_LIMIT
  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeBidsFragment_SetupTime")
    fragmentSetupTrace?.start()
    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      /* remove user transactions and fetch again */
      refreshData()
    }

    viewModel.offerLiveData.observe(viewLifecycleOwner, Observer {
      adapter.notifyDataSetChanged()
    })

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      //addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticData())

    /* observe and update adapter items */
    viewModel.userBidsData.reobserve(this, Observer {
      userPrefs.activeBidCount=viewModel.activeBids
      userPrefs.lostBidCount=viewModel.lostBids
      userPrefs.confirmedBidCount=viewModel.confirmedBids
     if(launch) {
       analyticsUtil.moEngageTrackEvent(
               EVENT_VIEW_BIDS_SCREEN,
               mutableListOf(PROPERTY_USER_ID, PROPERTY_ACTIVE_BIDS, PROPERTY_CONFIRMED_BIDS, PROPERTY_LOST_BIDS),
               mutableListOf(userPrefs.userId(), viewModel.activeBids, viewModel.confirmedBids, viewModel.lostBids)
       )

       val c = Date()
       val date = c.toString()
       analyticsUtil.moEngageTrackEvent(
         EVENT_VIEW_BIDS_SCREEN_OFFERS,
         mutableListOf(
           PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_NUMBER_OF_OFFERS,
           PROPERTY_DATE
         ),
         mutableListOf(
           userPrefs.userId(), userPrefs.phoneNumber!!,
           userPrefs.bidOfferCount.toString(), date
         )
       )
       launch=false
     }
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.bidsCountLiveData.reobserve(this, Observer {
      userPrefs.totalBidCount=it.toString()
      _title = when (it) {
        0, null -> getString(string.label_my_bids)
        else -> "${getString(string.label_my_bids)}($it)"
      }
    })

    viewModel.dataLoadingLiveData.reobserve(this, Observer {
      isLoadingData = it ?: false
    })

    /* attach sticky search with adapter */
    //binding.editStickySearch.attachWithAdapter(adapter, this)

    /* fetch bids data*/
    fetchBidsData(BidType.ActiveBid)
  }

  override fun onResume() {
    super.onResume()
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }
  }
  private fun fetchBidsData(bidType: BidType) {
    //set bidType
    viewModel.bidType = bidType
    //
    viewModel.fetchBidsSummary(bidType = bidType) // bids counts are fetched from this api
    //
    viewModel.fetchBids(bidType = bidType) //pass specific status to fetch ongoing/ won/ lost
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData(activeBidCount = viewModel.activeBids, confirmedBidCount = viewModel.confirmedBids, lostBidCount = viewModel.lostBids, bidType = viewModel.bidType)
    /* fetch again */
    fetchBidsData(viewModel.bidType)
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
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.moEngageTrackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        Log.i("itemDailog", "clicked")
        if(_item.requestType==RequestType.Contract.type){
          startActivity(_item.transactionId?.let { context?.let { it1 ->
            contractDetailsIntent(it,
              it1
            )
          } })
        }else{
          startActivity(_item.transactionId?.let { context?.let { it1 -> bidDetailsIntent(it, it1) } })

        }
      }

      HomeBidsRequestAction_PlaceBid -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.moEngageTrackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        Log.i("itemDailog", "clicked")
        bidDialog(_item)
      }

      HomeBidsRequestAction_ReviseBid -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.moEngageTrackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        Log.i("itemDailog", "clicked")
        bidDialog(_item)
      }

      HomeBidsHeaderAction_TabChangeActive -> {
        // Handle tab change to active bids
        viewModel.bidType = BidType.ActiveBid
        refreshData()
      }

      HomeBidsHeaderAction_TabChangeConfirmed -> {
        // Handle tab change to confirmed bids
        viewModel.bidType = BidType.ConfirmedBid
        refreshData()
      }

      HomeBidsHeaderAction_TabChangeLost -> {
        // Handle tab change to lost bids
        viewModel.bidType = BidType.LostBid
        refreshData()
      }

      HomeBidsSearchAction_Search -> {
        // Handle search action - get query and apply filter
        val searchItem = item as HomeBidsSearchItem
        val query = searchItem.data.query
        Log.d("SearchDebug", "Search action triggered with query: $query")
        
        if (!query.isNullOrEmpty()) {
          // Only filter if the query has changed to avoid unnecessary updates
          if (!adapter.checkFiltering() || adapter.getCurrentFilterQuery() != query) {
            Log.d("SearchDebug", "Applying filter with query: $query")
            val result = adapter.filter(query)
            Log.d("SearchDebug", "Filter result: $result, filtered items: ${adapter.itemsList().size}")
            
            // Test: Show a toast with the result
            //android.widget.Toast.makeText(context, "Search: $query, Results: ${adapter.itemsList().size}", android.widget.Toast.LENGTH_SHORT).show()
          } else {
            Log.d("SearchDebug", "Skipping filter - same query")
          }
        } else {
          Log.d("SearchDebug", "Query is null or empty")
        }
      }

      HomeBidsSearchAction_Clear -> {
        // Handle clear search action
        Log.d("SearchDebug", "Clearing search")
        adapter.cancelFilter()
        //android.widget.Toast.makeText(context, "Search cleared", android.widget.Toast.LENGTH_SHORT).show()
      }

      HomeBidsWarningAction_NoBids -> {
        action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
      }

      HomeBidsTimeOutAction -> {
        refreshData()
      }

      else -> {
        // Handle other actions
      }
    }
  }

  private fun bidDialog(transaction: HomeBidsRequestItemData? = null) {
      //  binding.transaction?.let {
          BulkBidDetailsDialog(
            requireContext(), transaction!!,transaction.bulkTransactionBids,viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs
          ).show()
      //  }
  }
  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_NO_ROUTES -> {
        if (resultCode == RESULT_OK) {
          action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
        }
      }
      else -> {

      }
    }
  }

  override fun getTotalOffers(data: HomeBidsRequestItemData?) {

    Executors.newSingleThreadExecutor().execute(Runnable {
      viewModel.fetchDatabaseOffers(data)
    })

  }

  override fun callShareRate(data: HomeBidsRequestItemData?, itemTD: String?, offerTD: String?, occ:String?, dcc:String?, offerid:String?,amount:String?) {
    val bundle = Bundle()
    bundle.putString("originname", data?.origin)
    bundle.putString("destname", data?.destination)
    bundle.putString("occ", occ)
    bundle.putString("dcc", dcc)
    bundle.putString("truckNumber", data?.transactionBid?.vehicleNumber)
    bundle.putString("truckType", data?.truckSpecification?.truckDispName)
    bundle.putString("truckCapacity", data?.truckCapacity())
    bundle.putString("itemTD", itemTD)
    bundle.putString("offerTD", offerTD)
    bundle.putString("offerid", offerid)
    bundle.putString("amt", amount)

    analyticsUtil.moEngageTrackEvent(
            EVENT_CLICKED_OFFER,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_SOURCE, PROPERTY_OFFER_ID),
            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", "bid_screen", offerid?:"")
    )
    navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
      //
    override fun loadMore() = viewModel.fetchBids(bidType = viewModel.bidType, paginate = true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  inner class HomeBidsRVScrollListener(
    private val stickyView: DelhiveryBidAnimatedSearchBar,
    private val elevation: Float = 12f
  ) : OnScrollListener() {

    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (!adapter.checkFiltering()) {
        val layoutManager =
          (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
        try {
        val pos = layoutManager.findFirstVisibleItemPosition()
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
      }catch (e:Exception){

      }

      }
    }
  }
}
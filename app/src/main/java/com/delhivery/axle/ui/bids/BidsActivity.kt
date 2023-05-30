package com.delhivery.axle.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.*
import com.delhivery.axle.ui.bids.BidType.ContractBid
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.home.activity.home.OFF_SET_LIMIT
import com.delhivery.axle.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapter
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterInterface
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Bid listing screen basis [BidType]
 */
class BidsActivity : BaseActivity<ActivityBidsBinding, BidsViewModel>(),
    HomeBidsRVAdapterInterface {

  init {
    hasInlineProgress = true
  }
  @Inject
  lateinit var userPrefs: UserPrefs

  var limit = OFF_SET_LIMIT

  override fun getViewModelClass() = BidsViewModel::class.java

  override fun layoutId() = R.layout.activity_bids

  override fun requireConnection() = true

  var isLoadingData = true

  /* search menu item ref */
  private var searchItem: MenuItem? = null

  /* rv adapter */
  private val adapter by lazy {
    HomeBidsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent == null || !intent.hasExtra(IntentExtraBidTypeKey)) {
      throw IllegalArgumentException("$IntentExtraBidTypeKey intent key missing")
    }

    /* get bid type from intent */
    viewModel.bidType =
      BidType.byTypeId(intent.getIntExtra(IntentExtraBidTypeKey, BidType.Unknown.typeId))

    if(viewModel.bidType==ContractBid){
      analyticsUtil.moEngageTrackEvent(
        EVENT_VIEW_CONTRACT_STATUS,mutableListOf(
          PROPERTY_USER_ID, PROPERTY_PHONE_NO
        ),
        mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:""))
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = viewModel.bidType.toolbarTitle()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.progressLiveData.observe(
        this, Observer { if (it == true) searchItem?.isVisible = false })

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    viewModel.offerLiveData.observe(this, Observer {
      adapter.notifyDataSetChanged()
    })

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@BidsActivity)
      adapter = this@BidsActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
      add(0, HomeBidsProgressItem())
    })

    /* bids observer */
    viewModel.bidsLiveData.observe(this, Observer {
      title = viewModel.bidType.toolbarTitle(viewModel.total)
      searchItem?.isVisible = it != null
      if (it != null) {
        adapter.operation(it)
      }
    })

    viewModel.bidsCountLiveData.observe(this, Observer {
      title = viewModel.bidType.toolbarTitle(it ?: 0)
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.fetchBids(false)

   /* viewModel.transactionBidLiveData.observe(this, Observer {
      when (it) {
        is BidDetailsUserBidState_BulkLoad_Edit -> {

        }
        else -> null
      }
    })*/
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData()
    /* fetch again */
    viewModel.fetchBids(false)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      OPEN_CONFIRMED_BID -> {

      }
      HomeBidsWarningAction_NoBids -> {
        setResult(RESULT_OK)
        finish()
      }

      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )

        if(_item.requestType=="contract"){
          if(_item.transactionId!=null) {
            userPrefs.setPreviousScreen( this.javaClass.name)
            startActivity(contractDetailsIntent(_item.transactionId, this, VALUE_BID_LISTING))
          }
          else{
            Toast.makeText(this,"Not Found", Toast.LENGTH_SHORT).show()
          }
        }else{
          val dmtStatus = if(_item.isDMTIndent())
            "dmt"
          else ""

          val active = dmtStatus =="dmt" && _item.bidStatus().status == "Active"
          val id = if(dmtStatus =="dmt" && (_item.bidStatus().status == "Confirmed" ||_item.bidStatus().status == "Lost"|| _item.bidStatus().status == "Cancelled"))
            _item.transactionBid!!.childTransactionId else _item.key()
          if(id!=null) {
            userPrefs.setPreviousScreen( this.javaClass.name)
            startActivity(bidDetailsIntent(id, this, dmtStatus, true, active))
          }
          else{
            Toast.makeText(this,"Not Found", Toast.LENGTH_SHORT).show()
          }
        }
      }
      HomeBidsRequestAction_ViewOtherDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        Log.i("itemDailog", "clicked")
        bidDialog(_item)
      }

      HomeBidsTimeOutAction ->
        refreshData()
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

    analyticsUtil.trackEvent(
            EVENT_CLICKED_OFFER,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_SOURCE, PROPERTY_OFFER_ID),
            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", "bid_screen", offerid?:"")
    )

    navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
  }

  private fun bidDialog(transaction: HomeBidsRequestItemData? = null) {
    //  binding.transaction?.let {
    /* set transaction id */
    viewModel.transactionId = transaction?.transactionId ?: ""
    viewModel.transaction = transaction!!
     // viewModel.fetchTransactionBids()
    BulkBidDetailsDialog(
      this@BidsActivity,  viewModel.transaction, transaction.bulkTransactionBids,viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs
    ).show()

  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_search, menu)
    val searchItem = menu?.findItem(R.id.action_search)
    val searchView = searchItem?.actionView as SearchView?
    setupSearch(searchItem, searchView)
    return true
  }

  /**
   * Setup search
   */
  private fun setupSearch(
    searchItem: MenuItem?,
    searchView: SearchView?
  ) {
    this.searchItem = searchItem
    searchItem?.isVisible = !binding.refreshLayout.isRefreshing

    /* Search query interface */
    searchView?.setOnQueryTextListener(object : OnQueryTextListener {
      override fun onQueryTextSubmit(p0: String?): Boolean {
        uiUtils.toggleKeyboard()
        return false
      }

      override fun onQueryTextChange(q: String?) = adapter.filter(q)
    })

    /* search bar expanded/collapse callbacks */
    searchItem?.setOnActionExpandListener(object : OnActionExpandListener {
      override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
        binding.refreshLayout.isEnabled = false
        adapter.enableFilter()
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_SEARCH_LOCAL,
            mutableListOf(PROPERTY_TRANSACTION_TYPE),
            mutableListOf(VALUE_BID)
        )
        return true
      }

      override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
        uiUtils.toggleKeyboard()
        binding.refreshLayout.isEnabled = true
        adapter.cancelFilter()
        return true
      }
    })
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchBids(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }
}

/*  */
private const val IntentExtraBidTypeKey = "bid_type"

/**
 * Get [BidsActivity] for specific [BidType] as [type]
 */
fun userBidsIntent(
  context: Context,
  type: BidType
) = Intent(context, BidsActivity::class.java).apply {
  putExtra(IntentExtraBidTypeKey, type.typeId)
}
package com.delhivery.axle.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.HomeBidsTimeOutAction
import com.delhivery.axle.data.home.bids.HomeBidsWarningAction_NoBids
import com.delhivery.axle.databinding.ActivityBidsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapter
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterInterface
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOCAL
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.VALUE_BID

/**
 * Bid listing screen basis [BidType]
 */
class BidsActivity : BaseActivity<ActivityBidsBinding, BidsViewModel>(),
    HomeBidsRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

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

        startActivity(bidDetailsIntent(_item.key(), this))
      }

      HomeBidsTimeOutAction ->
        refreshData()
    }
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

    override fun hasMore() = viewModel.offset < viewModel.total

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
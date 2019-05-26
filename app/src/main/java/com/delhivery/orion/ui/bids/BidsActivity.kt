package com.delhivery.orion.ui.bids

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityBidsBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.bids.BidType.ActiveBid
import com.delhivery.orion.ui.bids.BidType.LostBid
import com.delhivery.orion.ui.bids.BidType.Unknown
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.orion.utils.PaginationScrollListener
import com.delhivery.orion.utils.extensions.progressLiveData

class BidsActivity : BaseActivity<ActivityBidsBinding, BidsViewModel>(),
    ItemClickListener<HomeBidsRequestItem> {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = BidsViewModel::class.java

  override fun layoutId() = R.layout.activity_bids

  override fun requireConnection() = true

  /* search menu item ref */
  private var searchItem: MenuItem? = null

  /* rv adapter */
  private val adapter by lazy {
    BidsRVAdapter(this)
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

    binding.refreshLayout.progressLiveData(viewModel.progressLiveData, this)
    viewModel.progressLiveData.observe(
        this, Observer { if (it == true) searchItem?.isVisible = false })

    binding.refreshLayout.setOnRefreshListener {
      adapter.clearItems()
      /* remove user bid transactions and fetch again */
      viewModel.fetchBids(false)
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(this@BidsActivity)
      adapter = this@BidsActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    binding.btnStartBidding.setOnClickListener { finish() }

    /* bids observer */
    viewModel.bidsLiveData.observe(this, Observer {
      title = viewModel.bidType.toolbarTitle(viewModel.total)
      binding.error = it == null
      searchItem?.isVisible = it != null
      if (it != null) {
        adapter.operation(it)
      }
    })

    viewModel.fetchBids(false)
  }

  override fun onItemClicked(item: HomeBidsRequestItem) =
    startActivity(bidDetailsIntent(item.data, this))

  private fun onFabMenuItemSelected(item: DelhiveryFabCardMenuItem) {
    when (item.id) {
      0 -> ActiveBid
      1 -> LostBid
      else -> Unknown
    }.let { _type ->
      if (_type != viewModel.bidType) {
        viewModel.bidType = _type
        title = _type.toolbarTitle()
        viewModel.fetchBids(false)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_bids_activity, menu)
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

    override fun isLoading() = binding.refreshLayout.isRefreshing
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
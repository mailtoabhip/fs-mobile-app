package com.delhivery.orion.ui.bids

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.delhivery.orion.R
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.databinding.ActivityBidsBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.bids.BidType.ActiveBid
import com.delhivery.orion.ui.bids.BidType.LostBid
import com.delhivery.orion.ui.bids.BidType.Unknown
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem

class BidsActivity : BaseActivity<ActivityBidsBinding, BidsViewModel>() {

  override fun getViewModelClass() = BidsViewModel::class.java

  override fun layoutId() = R.layout.activity_bids

  override fun requireConnection() = true

  /* search menu item ref */
  private var searchItem: MenuItem? = null

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

    /* Use this logic to create our own menu as per ui */
    binding.fabFilter.setOnClickListener { fab ->
      uiUtils.fabCardMenu(fab as FloatingActionButton, BidsFabCardMenuItems) {
        onFabMenuItemSelected(it)
      }
    }

    /* bids observer */
    viewModel.bidsLiveData.observe(this, BidsObserver())

    viewModel.fetchBids()
  }

  private fun onFabMenuItemSelected(item: DelhiveryFabCardMenuItem) {
    when (item.id) {
      0 -> ActiveBid
      1 -> LostBid
      else -> Unknown
    }.let { _type ->
      if (_type != viewModel.bidType) {
        viewModel.bidType = _type
        title = _type.toolbarTitle()
        viewModel.fetchBids()
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
   * Setup zone search
   */
  private fun setupSearch(
    searchItem: MenuItem?,
    searchView: SearchView?
  ) {
    this.searchItem = searchItem
//    searchItem?.isVisible = viewModel.inlineProgressLiveData.value == false
//
//    /* Search query interface */
//    searchView?.setOnQueryTextListener(object : OnQueryTextListener {
//      override fun onQueryTextSubmit(p0: String?): Boolean {
//        uiUtils.toggleKeyboard()
//        return false
//      }
//
//      override fun onQueryTextChange(p0: String?): Boolean {
//        viewModel.search(p0)
//        return false
//      }
//    })
//
//    /* search bar expanded/collapse callbacks */
//    searchItem?.setOnActionExpandListener(object : OnActionExpandListener {
//      override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
//        binding.swipeRefresh.isEnabled = false
//        binding.spinnerType.visible(false)
//        menu?.setGroupVisible(R.id.mgrp_scan_type, false)
//        return true
//      }
//
//      override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
//        viewModel.filteredResults()
//        uiUtils.toggleKeyboard()
//        binding.swipeRefresh.isEnabled = true
//        binding.spinnerType.visible(true)
//        menu?.setGroupVisible(R.id.mgrp_scan_type, true)
//        return true
//      }
//    })
  }

  /**
   * Bids observer
   */
  inner class BidsObserver : Observer<Pair<Int, List<TransactionBid>>> {
    override fun onChanged(t: Pair<Int, List<TransactionBid>>?) {
      t?.apply {
        title = viewModel.bidType.toolbarTitle(first)
      }
    }
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
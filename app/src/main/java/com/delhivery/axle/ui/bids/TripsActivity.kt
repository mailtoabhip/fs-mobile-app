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
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.ActivityTripsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterInterface
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Warning
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOCAL
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.VALUE_BID
import com.delhivery.axle.utils.VALUE_TRIP

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Displays listing of all trips of a particular type on basis of selected header from
 * [HomeTripsFragment].
 *
 **
 */
class TripsActivity : BaseActivity<ActivityTripsBinding, TripsViewModel>(),
    HomeTripsRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = TripsViewModel::class.java

  override fun layoutId() = R.layout.activity_trips

  override fun requireConnection() = true

  var isLoadingData = true

  /* search menu item ref */
  private var searchItem: MenuItem? = null

  /* rv adapter */
  private val adapter by lazy {
    TripsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (intent == null || !intent.hasExtra(IntentExtraTripTypeKey)) {
      throw IllegalArgumentException("$IntentExtraTripTypeKey intent key missing")
    }

    /* get bid type from intent */
    viewModel.trip =
      TripType.byTypeId(intent.getIntExtra(IntentExtraTripTypeKey, TripType.Unknown.typeId))
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = viewModel.trip.toolbarTitle()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.progressLiveData.observe(
        this, Observer { if (it == true) searchItem?.isVisible = false })

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TripsActivity)
      adapter = this@TripsActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticItems())

    viewModel.userTripsData.observe(this, Observer {
      if (it != null) {
        adapter.operation(it)
      }
    })

    viewModel.tripsCountLiveData.observe(this, Observer {
      title = viewModel.trip.toolbarTitle(it ?: 0)
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.fetchTrips(false)
  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsProgressItem())
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData()
    /* fetch again */
    viewModel.fetchTrips(false)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (item.type) {
      TripItem -> {
        val _item = item.data as HomeTripsItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_TRIP, _item.transactionId)
        )
        startActivity(tripDetailsIntent(_item, this))
      }
      Timeout -> {
        refreshData()
      }
      Warning -> {
        finish()
      }
      else -> {
      }
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
            mutableListOf(VALUE_TRIP)
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
    override fun loadMore() = viewModel.fetchTrips(true)

    override fun hasMore() = viewModel.offset < viewModel.total

    override fun isLoading() = isLoadingData
  }

}

/*  */
private const val IntentExtraTripTypeKey = "trip_type"

/**
 * Get [TripsActivity] for specific [TripsType] as [type]
 */
fun userTripsIntent(
  context: Context,
  type: TripType
) = Intent(context, TripsActivity::class.java).apply {
  putExtra(IntentExtraTripTypeKey, type.typeId)
}
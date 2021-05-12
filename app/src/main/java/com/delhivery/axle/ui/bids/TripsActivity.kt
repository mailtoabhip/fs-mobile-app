package com.delhivery.axle.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutAction
import com.delhivery.axle.data.home.trips.HomeTripsWarningAction_NoLoads
import com.delhivery.axle.databinding.ActivityTripsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.bids.TripType.Companion
import com.delhivery.axle.ui.dialogs.TripsFilterDialog
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapter
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterInterface
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOCAL
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_TRIP
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Displays listing of all trips of a particular type on
 * basis of trip status
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
    HomeTripsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      require(
          !(intent == null || !intent.hasExtra(IntentExtraViewTypeKey))
      ) { "$IntentExtraViewTypeKey intent key missing" }
    } catch (e: IllegalArgumentException) {
      finish()
    }

    viewModel.viewType = intent.getStringExtra(IntentExtraViewTypeKey)

    if (viewModel.viewType != "all") {
      try {
        require(
            !(intent == null || !intent.hasExtra(IntentExtraSubViewTypeKey))
        ) { "$IntentExtraSubViewTypeKey intent key missing" }
      } catch (e: IllegalArgumentException) {
        finish()
      }

      /* get su view type from intent */
      val subview = intent.getIntExtra(IntentExtraSubViewTypeKey, 0)
      if (viewModel.viewType.equals("trips_view")) {
        viewModel.tripType = TripType.byTypeId(subview)
      } else {
        viewModel.viewPaymentType = ViewPaymentType.byTypeId(subview)
      }
    }

  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = when {
      viewModel.viewType.equals("all") -> "All Trips"
      viewModel.viewType.equals("payment_view") -> {
        viewModel.viewPaymentType.toolbarTitle()
      }
      else -> {
        viewModel.tripType.toolbarTitle()
      }
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    setHeaderResources()

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

    viewModel.summaryLiveData.observe(this, Observer {
      binding.textAdvancePending.text = it.advancePending.amount()
      binding.textBalancePending.text = it.balancePending.amount()
      binding.textRecoveryPending.text = it.recoveryPending.amount()

      val totalPending = (it.advancePending.amount ?: 0.0) + (it.balancePending.amount ?: 0.0) + (it.recoveryPending.amount ?: 0.0)
      val totalPendingText = "Total Pending: ₹ " + StringUtils.formatAmount(totalPending)
      binding.txtTotalPending.text = totalPendingText

      binding.textArrival.text = it.awaitingArrival.count()
      binding.textInTransit.text = it.inTransit.count()
      binding.textAwaitingLoading.text = it.awaitingLoading.count()
      binding.textAwaitingUnloading.text = it.awaitingUnloading.count()
    })

    viewModel.tripsCountLiveData.observe(this, Observer {
      val count = it ?: 0
      title = when {
        viewModel.viewType.equals("all") -> "All Trips (${count})"
        viewModel.viewType.equals("payment_view") -> {
          viewModel.viewPaymentType.toolbarTitle(count)
        }
        else -> {
          viewModel.tripType.toolbarTitle(count)
        }
      }

      binding.txtTripCount.text = viewModel.tripsCountText
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.filterAppliedLiveData.observe(this, Observer {
      fetchTripDetails()
    })

    binding.viewAdvancePending.setOnClickListener {
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(0)
      setHeaderResources()
      refreshData()
    }

    binding.viewBalancePending.setOnClickListener {
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(1)
      setHeaderResources()
      refreshData()
    }

    binding.viewRecoveryPending.setOnClickListener {
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(2)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingArrival.setOnClickListener {
      viewModel.tripType = Companion.byTypeId(0)
      setHeaderResources()
      refreshData()
    }

    binding.viewInTransit.setOnClickListener {
      viewModel.tripType = Companion.byTypeId(1)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingLoading.setOnClickListener {
      viewModel.tripType = Companion.byTypeId(2)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingUnloading.setOnClickListener {
      viewModel.tripType = Companion.byTypeId(3)
      setHeaderResources()
      refreshData()
    }

    binding.filterIcon.setOnClickListener {
      val filter1 = "All Trips " + "(" + viewModel.total.toString() + ")"
      val filter2 = "Trips with the issue " + "(" + viewModel.issueTripsCount.toString() + ")"
      TripsFilterDialog(this, filter1, filter2, viewModel).show()
    }

    refreshData()
  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsProgressItem())
  }

  private fun fetchTripDetails() {
    adapter.setItems(getStaticItems())
    viewModel.fetchTrips(false)
  }

  private fun refreshData() {
    viewModel.fetchTripsSummary()
    fetchTripDetails()
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as HomeTripsItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_TRIP, data.transactionId)
        )
        startActivity(tripDetailsIntent(data.key(), this, viewModel.tripType.typeText))
      }
      HomeTripsTimeOutAction -> {
        refreshData()
      }
      HomeTripsWarningAction_NoLoads -> {
        setResult(RESULT_OK)
        finish()
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

  private fun setHeaderResources() {
    when {
      viewModel.viewType.equals("trips_view") -> {

        binding.paymentsFilterView.visibility = View.GONE
        binding.tripsFilterView.visibility = View.VISIBLE
        binding.txtTotalPending.visibility = View.GONE

        when (viewModel.tripType) {
          Companion.byTypeId(0) -> {

            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(resources.getColor(R.color.white))
            binding.textArrival.setTextColor(resources.getColor(R.color.white))

            binding.idInTransit.setTextColor(resources.getColor(R.color.black))
            binding.textInTransit.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingLoading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingLoading.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingUnloading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingUnloading.setTextColor(resources.getColor(R.color.black))

          }
          Companion.byTypeId(1) -> {

            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(resources.getColor(R.color.black))
            binding.textArrival.setTextColor(resources.getColor(R.color.black))

            binding.idInTransit.setTextColor(resources.getColor(R.color.white))
            binding.textInTransit.setTextColor(resources.getColor(R.color.white))

            binding.idAwaitingLoading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingLoading.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingUnloading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingUnloading.setTextColor(resources.getColor(R.color.black))

          }
          Companion.byTypeId(2) -> {

            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(resources.getColor(R.color.black))
            binding.textArrival.setTextColor(resources.getColor(R.color.black))

            binding.idInTransit.setTextColor(resources.getColor(R.color.black))
            binding.textInTransit.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingLoading.setTextColor(resources.getColor(R.color.white))
            binding.textAwaitingLoading.setTextColor(resources.getColor(R.color.white))

            binding.idAwaitingUnloading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingUnloading.setTextColor(resources.getColor(R.color.black))

          }
          else -> {

            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_all_4_corner_white)

            binding.idArrival.setTextColor(resources.getColor(R.color.black))
            binding.textArrival.setTextColor(resources.getColor(R.color.black))

            binding.idInTransit.setTextColor(resources.getColor(R.color.black))
            binding.textInTransit.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingLoading.setTextColor(resources.getColor(R.color.black))
            binding.textAwaitingLoading.setTextColor(resources.getColor(R.color.black))

            binding.idAwaitingUnloading.setTextColor(resources.getColor(R.color.white))
            binding.textAwaitingUnloading.setTextColor(resources.getColor(R.color.white))

          }
        }
      }
      viewModel.viewType.equals("payment_view") -> {

        binding.paymentsFilterView.visibility = View.VISIBLE
        binding.tripsFilterView.visibility = View.GONE
        binding.txtTotalPending.visibility = View.VISIBLE

        when (viewModel.viewPaymentType) {
          ViewPaymentType.byTypeId(0) -> {

            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.id1.setTextColor(resources.getColor(R.color.white))
            binding.textAdvancePending.setTextColor(resources.getColor(R.color.white))

            binding.id2.setTextColor(resources.getColor(R.color.black))
            binding.textBalancePending.setTextColor(resources.getColor(R.color.black))

            binding.id3.setTextColor(resources.getColor(R.color.black))
            binding.textRecoveryPending.setTextColor(resources.getColor(R.color.black))

          }
          ViewPaymentType.byTypeId(1) -> {

            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.id1.setTextColor(resources.getColor(R.color.black))
            binding.textAdvancePending.setTextColor(resources.getColor(R.color.black))

            binding.id2.setTextColor(resources.getColor(R.color.white))
            binding.textBalancePending.setTextColor(resources.getColor(R.color.white))

            binding.id3.setTextColor(resources.getColor(R.color.black))
            binding.textRecoveryPending.setTextColor(resources.getColor(R.color.black))

          }
          else -> {

            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_all_4_corner_white)

            binding.id1.setTextColor(resources.getColor(R.color.black))
            binding.textAdvancePending.setTextColor(resources.getColor(R.color.black))

            binding.id2.setTextColor(resources.getColor(R.color.black))
            binding.textBalancePending.setTextColor(resources.getColor(R.color.black))

            binding.id3.setTextColor(resources.getColor(R.color.white))
            binding.textRecoveryPending.setTextColor(resources.getColor(R.color.white))

          }
        }

      }
      else -> {

        binding.paymentsFilterView.visibility = View.GONE
        binding.tripsFilterView.visibility = View.GONE
        binding.txtTotalPending.visibility = View.GONE

      }
    }
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
private const val IntentExtraViewTypeKey = "view_type"
private const val IntentExtraSubViewTypeKey = "trip_type"

/**
 * Get [TripsActivity] for specific [viewType] as [String] and [TripType] as [type]
 */
fun userTripsIntent(
  context: Context,
  viewType: String,
  subview: Int
) = Intent(context, TripsActivity::class.java).apply {
  putExtra(IntentExtraViewTypeKey, viewType)
  putExtra(IntentExtraSubViewTypeKey, subview)
}
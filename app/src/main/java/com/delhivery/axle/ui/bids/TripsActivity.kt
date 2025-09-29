package com.delhivery.axle.ui.bids

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.databinding.ActivityTripsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.bids.TripType.Companion
import com.delhivery.axle.ui.dialogs.TripsFilterDialog
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.home.fragments.trips.BaseHomeTripsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsProgressItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapter
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterInterface
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.data.home.trips.*
import com.delhivery.axle.ui.dialogs.ChangePaymentModeDialog
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.*
import javax.inject.Inject

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
   var intentRefresh= false
   var finalTime :Long = 0
   var tripDataItem : HomeTripsItemData? = null
   var itemPos = 0

  @Inject lateinit var userPrefs :UserPrefs
  /* search menu item ref */
  private var searchItem: MenuItem? = null
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  /* rv adapter */
  private val adapter by lazy {
    HomeTripsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("TripsActivity_SetupTime")
    activitySetupTrace?.start()
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

  @RequiresApi(VERSION_CODES.N)
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
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }

    setHeaderResources()

    viewModel.progressLiveData.observe(
        this, Observer { if (it == true) searchItem?.isVisible = false })

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData(true)
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
        viewModel.tripsFilter = ""
        viewModel.loadMore()
      }
    })

    viewModel.summaryLiveData.observe(this, Observer {
      binding.textAdvancePending.text = it.advancePending.amount()
      binding.textAdvancePendingCount.text = it.advancePending.count()
      binding.textBalancePending.text = it.balancePending.amount()
      binding.textBalancePendingCount.text = it.balancePending.count()
      binding.textRecoveryPending.text = it.recoveryPending.amount()
      binding.textRecoveryPendingCount.text = it.recoveryPending.count()

      val totalPending = (it.advancePending.amount ?: 0.0) + (it.balancePending.amount ?: 0.0) + (it.recoveryPending.amount ?: 0.0)
      val totalPendingText = "Total Pending: ₹ " + StringUtils.formatAmount(totalPending)
      binding.txtTotalPending.text = totalPendingText

      binding.textArrival.text = it.awaitingArrival.count()
      binding.textInTransit.text = it.inTransit.count()
      binding.textAwaitingLoading.text = it.awaitingLoading.count()
      binding.textAwaitingUnloading.text = it.awaitingUnloading.count()
      binding.textAwaitingPod.text = it.awaitingPod.count()

      if(!intentRefresh) {

        finalTime = Date().time
        val loadingTime: String = ((finalTime - userPrefs.startTime) / 1000).toInt().toString() + "sec"

        if (viewModel.viewType.equals("trips_view")) {
          when (viewModel.tripType) {
            Companion.byTypeId(0) -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_TRIPS_AWAITING_ARRIVAL,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRIPS_AWAITING_ARRIVAL_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.awaitingArrival.count(), loadingTime)
              )
            }
            Companion.byTypeId(1) -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_TRIPS_INTRANSIT,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRIPS_INTRANSIT_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.inTransit.count(), loadingTime)
              )
            }
            Companion.byTypeId(2) -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_TRIPS_AWAITING_LOADING,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRIPS_AWAITING_LOADING_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.awaitingLoading.count(), loadingTime)
              )
            }
            else -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_TRIPS_AWAITING_UNLOADING,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_TRIPS_AWAITING_UNLOADING_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.awaitingUnloading.count(), loadingTime)
              )
            }
          }
        } else if (viewModel.viewType.equals("payment_view")) {
          when (viewModel.viewPaymentType) {
            ViewPaymentType.byTypeId(0) -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_ADVANCE_PENDING,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_ADVANCE_PENDING_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.advancePending.count(), loadingTime)
              )
            }
            ViewPaymentType.byTypeId(1) -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_BALANCE_PENDING,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_BALANCE_PENDING_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.balancePending.count(), loadingTime)
              )

            }
            else -> {
              analyticsUtil.moEngageTrackEvent(
                      EVENT_VIEW_RECOVERY_PENDING,
                      mutableListOf(PROPERTY_USER_ID, PROPERTY_RECOVERY_PENDING_COUNT, PROPERTY_LOADING_TIME),
                      mutableListOf(userPrefs.userId(), it.recoveryPending.count(), loadingTime)
              )
            }
          }
        }
        else{
          val total = it.awaitingArrival.count!! + it.inTransit.count!! + it.awaitingPod.count!! + it .awaitingLoading.count!! + it.awaitingUnloading.count!!
          analyticsUtil.moEngageTrackEvent(
                  EVENT_VIEW_ALL_TRIPS,
                  mutableListOf(PROPERTY_USER_ID , PROPERTY_ALL_TRIPS_COUNT , PROPERTY_LOADING_TIME),
                  mutableListOf(userPrefs.userId() , total.toString() ,loadingTime)
          )
        }
      }
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
      if (viewModel.tripsFilter == "issue_trips") {
        var pendingBalanceCount = ""
        pendingBalanceCount = if (viewModel.tripsCount > 1) {
          "${viewModel.tripsCount} trips"
        } else {
          "${viewModel.tripsCount} trip"
        }
        val pendingBalanceAmount = "₹ ${StringUtils.formatAmount(viewModel.balancePendingTotal)}"
        val total = viewModel.advancePendingTotal + viewModel.balancePendingTotal + viewModel.recoveryPendingTotal
        val totalPendingAmount = "Total Pending: ₹ " + StringUtils.formatAmount(total)
        binding.textBalancePending.text = pendingBalanceAmount
        binding.textBalancePendingCount.text = pendingBalanceCount
        binding.txtTotalPending.text = totalPendingAmount
      }
//      if (viewModel.isSettledFilter) {
//        binding.txtTripCount.text = "All Trips (${viewModel.tripsCount})"
//        title = "All Trips (${viewModel.tripsCount})"
//      }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.filterAppliedLiveData.observe(this, Observer {
      refreshData()
    })

    viewModel.teamMembersLiveData.observe(this , Observer {
      uiUtils.hideProgress()
      if(it!= null){
        tripDataItem?.let { it1 -> ChangePaymentModeDialog(this,viewModel, it1, it, userPrefs, uiUtils, itemPos, analyticsUtil).show() }
        analyticsUtil.moEngageTrackEvent(
          EVENT_VIEW_CHANGE_PAYMENT_MODE_TRIPS,
          mutableListOf(PROPERTY_USER_ID),
          mutableListOf(userPrefs.userId())
        )
      }
    })

    viewModel.omcLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if( it != null){
        uiUtils.showProgress()
        viewModel.getOMCResult(it.first, it.second)
      }
    })

    viewModel.omcGetLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if(it!= null){
        if(it.first!= "") {
          uiUtils.showProgress()
          viewModel.updateTripWithFuelPayout(it.first, it.second)
        }
        else{
          uiUtils.showSnackbar("OMC not found")
        }
      }
    })

    viewModel.fuelPayoutLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if(it!= null){
        val data = adapter.itemsList()[it.second].data as? HomeTripsItemData
        data?.payment!!.apply {
          fuelPayout = it.third.first
          fuelNumber = it.third.second
        }
        uiUtils.showSnackbar(it.first)
        adapter.notifyItemChanged(it.second)
      }
    })


    binding.viewAdvancePending.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(0)
      setHeaderResources()
      refreshData()
    }

    binding.viewBalancePending.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(1)
      setHeaderResources()
      refreshData()
    }

    binding.viewRecoveryPending.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.viewPaymentType = ViewPaymentType.byTypeId(2)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingArrival.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.tripType = Companion.byTypeId(0)
      setHeaderResources()
      refreshData()
    }

    binding.viewInTransit.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.tripType = Companion.byTypeId(1)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingLoading.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.tripType = Companion.byTypeId(2)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingUnloading.setOnClickListener {
      userPrefs.startTime = Date().time
      viewModel.tripType = Companion.byTypeId(3)
      setHeaderResources()
      refreshData()
    }

    binding.viewAwaitingPod.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      startActivity(homeActivityIntent("pod", this))
    }

    binding.filterIcon.setOnClickListener {
      if (viewModel.filterList.isNotEmpty() && viewModel.filterList.size <= 4) {
        TripsFilterDialog(this, viewModel.filterList, viewModel,analyticsUtil,userPrefs,viewModel.filterKey).show()
      }
    }

    binding.llLoadedFilter.setOnClickListener {
      if (viewModel.loadingDateFilter) {
        binding.toggleRemovedLoadedFilter.visibility = View.GONE
        binding.loadedAfter.text = "Loaded after"
        viewModel.loadingDateFilter = false
        viewModel.date = -1
        viewModel.month = -1
        viewModel.year = -1
        fetchTripDetails()
      } else {
        binding.toggleRemovedLoadedFilter.visibility = View.VISIBLE
        viewModel.loadingDateFilter = true
        openDatePicker()
      }
    }

    binding.llSettledFilter.setOnClickListener {
      if (viewModel.isSettledFilter) {
        binding.toggleRemovedSettle.visibility = View.GONE
        viewModel.isSettledFilter = false
      } else {
        binding.toggleRemovedSettle.visibility = View.VISIBLE
        viewModel.isSettledFilter = true
      }
      fetchTripDetails()
    }

    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  @SuppressLint("SetTextI18n")
  @RequiresApi(Build.VERSION_CODES.N)
  private fun openDatePicker(){
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(this, {
      view, year, monthOfYear, dayOfMonth ->
      viewModel.date = dayOfMonth
      viewModel.month = monthOfYear
      viewModel.year = year
      var month = monthOfYear + 1
      viewModel.loadingDate =  "$dayOfMonth/$month/${year.toString().substring(2)}"
      binding.loadedAfter.text = "Loaded after: " + viewModel.loadingDate
      if (viewModel.loadingDate.isNotNullOrEmpty()) {
        analyticsUtil.moEngageTrackEvent(
                EVENT_FILTER_ALL_TRIPS,
                mutableListOf(PROPERTY_USER_ID , PROPERTY_LOADED_AFTER , PROPERTY_ONLY_SETTLED),
                mutableListOf(userPrefs.userId() , viewModel.loadingDate ,viewModel.isSettledFilter.toString())
        )
        fetchTripDetails()
      } else {
        uiUtils.showSnackbar("Please choose valid date")
      }

    }, year, month, day)

    datePickerDialog.setOnCancelListener {
      binding.toggleRemovedLoadedFilter.visibility = View.GONE
      binding.loadedAfter.text = "Loaded after"
      viewModel.loadingDateFilter = false
      viewModel.date = -1
      viewModel.month = -1
      viewModel.year = -1
    }

    datePickerDialog.show()

  }

  private fun getStaticItems() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsProgressItem())
  }

  private fun fetchTripDetails() {
    viewModel.request = SearchRequest()
    adapter.setItems(getStaticItems())
    viewModel.fetchTrips(false)
  }

  private fun refreshData(intent: Boolean = false) {
    intentRefresh = intent
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
        analyticsUtil.moEngageTrackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_TRIP, data.transactionId)
        )
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(tripDetailsIntent(data.key(), this, viewModel.tripType.typeText))
      }
      HomeTripsTimeOutAction -> {
        refreshData()
      }
      HomeTripsWarningAction_NoTrips -> {
        setResult(RESULT_OK)
        finish()
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeTripsRVAdapterItem<*>,
    position: Int) {
    when (actionId){
      HomeAdvancePendingPaymentMode ->{
        initChangePaymentMode(item.data as HomeTripsItemData, position)
    }
    }

  }

  private fun initChangePaymentMode(data: HomeTripsItemData ,position: Int) {
    tripDataItem = data
    itemPos = position
    uiUtils.showProgress()
    viewModel.fetchTeamMembers()
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
      override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
        binding.refreshLayout.isEnabled = false
        adapter.enableFilter()
        // Capture event
        analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_LOCAL,
            mutableListOf(PROPERTY_TRANSACTION_TYPE),
            mutableListOf(VALUE_TRIP)
        )
        return true
      }

      override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
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

        binding.llAllTripFilters.visibility = View.GONE
        binding.paymentsFilterView.visibility = View.GONE
        binding.tripsFilterView.visibility = View.VISIBLE
        binding.txtTotalPending.visibility = View.GONE

        when (viewModel.tripType) {
          Companion.byTypeId(0) -> {

            binding.filterIcon.visibility = View.GONE
            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textArrival.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.idInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))

          }
          Companion.byTypeId(1) -> {

            binding.filterIcon.visibility = View.VISIBLE
            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textArrival.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idInTransit.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textInTransit.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.idAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))

            viewModel.filterList = listOf("All", "Delayed")
            viewModel.filterKey = "loaded_after"

          }
          Companion.byTypeId(2) -> {

            binding.filterIcon.visibility = View.VISIBLE
            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.idArrival.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textArrival.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.idAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.black))

            viewModel.filterList = listOf("Less than 1 day", "1 day +", "2 days +", "3 days +")
            viewModel.filterKey = "arrived_ageing"

          }
          else -> {

            binding.filterIcon.visibility = View.VISIBLE
            binding.viewAwaitingArrival.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewInTransit.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingLoading.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewAwaitingUnloading.setBackgroundResource(R.drawable.bg_all_4_corner_white)

            binding.idArrival.setTextColor(ContextCompat.getColor(this, (R.color.black)))
            binding.textArrival.setTextColor(ContextCompat.getColor(this, (R.color.black)))

            binding.idInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textInTransit.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAwaitingLoading.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.idAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textAwaitingUnloading.setTextColor(ContextCompat.getColor(this, R.color.white))

            viewModel.filterList = listOf("Less than 1 day", "1 day +", "2 days +", "3 days +")
            viewModel.filterKey = "reached_ageing"

          }
        }
      }
      viewModel.viewType.equals("payment_view") -> {

        binding.llAllTripFilters.visibility = View.GONE
        binding.paymentsFilterView.visibility = View.VISIBLE
        binding.tripsFilterView.visibility = View.GONE
        binding.txtTotalPending.visibility = View.VISIBLE

        when (viewModel.viewPaymentType) {
          ViewPaymentType.byTypeId(0) -> {

            binding.filterIcon.visibility = View.GONE
            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.id1.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textAdvancePending.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textAdvancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.id2.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textBalancePending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textBalancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.id3.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textRecoveryPending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textRecoveryPendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

          }
          ViewPaymentType.byTypeId(1) -> {

            binding.filterIcon.visibility = View.VISIBLE
            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_all_4_corner_white)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_white_all_corner_black)

            binding.id1.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAdvancePending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAdvancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.id2.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textBalancePending.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textBalancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.id3.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textRecoveryPending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textRecoveryPendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

            viewModel.filterList = listOf("All", "Trips with POD issue")
            viewModel.filterKey = "trips_with_issue"

          }
          else -> {

            binding.filterIcon.visibility = View.GONE
            binding.viewAdvancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewBalancePending.setBackgroundResource(R.drawable.bg_white_all_corner_black)
            binding.viewRecoveryPending.setBackgroundResource(R.drawable.bg_all_4_corner_white)

            binding.id1.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAdvancePending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textAdvancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.id2.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textBalancePending.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.textBalancePendingCount.setTextColor(ContextCompat.getColor(this, R.color.black))

            binding.id3.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textRecoveryPending.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textRecoveryPendingCount.setTextColor(ContextCompat.getColor(this, R.color.white))

          }
        }

      }
      else -> {
        binding.filterIcon.visibility = View.GONE
        binding.llAllTripFilters.visibility = View.VISIBLE
        binding.paymentsFilterView.visibility = View.GONE
        binding.tripsFilterView.visibility = View.GONE
        binding.txtTotalPending.visibility = View.GONE

      }
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : DynamicPaginationScrollListener() {
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
package com.delhivery.axle.ui.home.fragments.trucks

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.trucks.FastagStats
import com.delhivery.axle.data.home.trucks.HomeTrucksAvailabilityFilterAction
import com.delhivery.axle.data.home.trucks.HomeTrucksPriorityAction
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestAction_ActivateTruck
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestAction_BuyFastag
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestAction_EditTruck
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.data.home.trucks.HomeTrucksSizeFilterAction
import com.delhivery.axle.data.home.trucks.HomeTrucksTimeOutAction
import com.delhivery.axle.data.home.trucks.HomeTrucksVehicleFilterAction
import com.delhivery.axle.data.home.trucks.HomeTrucksWarningAction_NoTrucks
import com.delhivery.axle.data.home.trucks.TruckFrequentItem
import com.delhivery.axle.databinding.DialogBottomTruckAddBinding
import com.delhivery.axle.databinding.DialogBottomTruckDeactivateBinding
import com.delhivery.axle.databinding.DialogBottomTruckFilterBinding
import com.delhivery.axle.databinding.DialogBottomTruckOptionsBinding
import com.delhivery.axle.databinding.DialogTooltipBinding
import com.delhivery.axle.databinding.FragmentHomeTrucksBinding
import com.delhivery.axle.databinding.ViewFrequentTruckItemBinding
import com.delhivery.axle.ui.dialogs.BuyFastagBottomSheetDialogFragment
import com.delhivery.axle.ui.dialogs.FastagSuccessBottomSheetDialogFragment
import com.delhivery.axle.ui.fastag.issuance.BuyFasTagActivity
import com.delhivery.axle.ui.home.activity.home.OFF_SET_LIMIT
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.ui.trucks.ActivateTruckDialog
import com.delhivery.axle.ui.trucks.EditTruckDialog
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_ADD_TRUCK_INITIATE
import com.delhivery.axle.utils.EVENT_BANNER_CLICK_TOP
import com.delhivery.axle.utils.EVENT_CLICKED_OFFER
import com.delhivery.axle.utils.EVENT_CLICKED_PRICE_BANNER
import com.delhivery.axle.utils.EVENT_DEACTIVATE_TRUCK
import com.delhivery.axle.utils.EVENT_DELETE_TRUCK
import com.delhivery.axle.utils.EVENT_EDIT_TRUCK_INITIATE
import com.delhivery.axle.utils.EVENT_EDIT_TRUCK_SUBMIT
import com.delhivery.axle.utils.EVENT_REQUEST_FOR_LOAD_SUBMIT
import com.delhivery.axle.utils.EVENT_TRUCKS_PAGE_SHOWN
import com.delhivery.axle.utils.EVENT_VIEW_MY_TRUCK
import com.delhivery.axle.utils.FCMUtils
import com.delhivery.axle.utils.PROPERTY_FASTAG_MAPPED
import com.delhivery.axle.utils.PROPERTY_FIELD_EDITED
import com.delhivery.axle.utils.PROPERTY_INVENTORY_ID
import com.delhivery.axle.utils.PROPERTY_INVENTORY_UUID
import com.delhivery.axle.utils.PROPERTY_OFFER_ID
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_REASON
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_TOTAL_COUNT
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_ADD_TRUCK
import com.delhivery.axle.utils.REQCODE_FASTAG_RECHARGE
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_PAGE
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_TOP_BANNER
import com.delhivery.axle.utils.VALUE_BANNER
import com.delhivery.axle.utils.VALUE_DEEP_LINKING
import com.delhivery.axle.utils.VALUE_DESTINATION
import com.delhivery.axle.utils.VALUE_MY_TRUCKS
import com.delhivery.axle.utils.VALUE_NOTIFICATION
import com.delhivery.axle.utils.VALUE_ORIGIN
import com.delhivery.axle.utils.VALUE_OWNERSHIP
import com.delhivery.axle.utils.VALUE_PRICE
import com.delhivery.axle.utils.VALUE_TRUCKS_PAGE
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.getTextChangeObservable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.concurrent.Executors
import javax.inject.Inject


class HomeTrucksFragment : HomeBaseFragment<FragmentHomeTrucksBinding, HomeTrucksViewModel>(),
        HomeTrucksRVAdapterInterface, TitleProvider
{
    override val title: CharSequence
        get() = "Trucks"

    override fun getViewModelClass() = HomeTrucksViewModel::class.java
    override fun layoutId() = R.layout.fragment_home_trucks

    var totalTruck: Int = 0
    var bannerValue:Boolean? = false
    var launch : Boolean =true
    val limit = OFF_SET_LIMIT

    companion object {
        /* singleton instance */
        val _instance: HomeTrucksFragment by lazy { HomeTrucksFragment() }
    }

    /* RV adapter */
    private val adapter: HomeTrucksRVAdapter by lazy {
        HomeTrucksRVAdapter(this)
    }


    init {
        toolbarElevationLiveData = MutableLiveData()
        hasInlineProgress = true
    }


    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var fcmUtils: FCMUtils
    @Inject lateinit var userPrefs: UserPrefs

    var scrollDist = 0
    var visible = false
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    private var isFirstLoad = true


    private fun sendAnalyticsEvent(stats: FastagStats) {
        analyticsUtil.moEngageTrackEvent(
            EVENT_TRUCKS_PAGE_SHOWN,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PAGE_NAME,
                PROPERTY_TOTAL_COUNT,
                PROPERTY_FASTAG_MAPPED
            ),
            mutableListOf(
                userPrefs.userId(),
                VALUE_TRUCKS_PAGE,
                stats.totalTrucks.toString(),
                stats.fastagTrucksCount.toString(),
            ),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeTrucksFragment_SetupTime")
        fragmentSetupTrace?.start()
        viewModel.fetchData()

        viewModel.fetchTruckType()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            binding.editStickySearch.clearFocus()
            hideKeyboard(binding.editStickySearch)
            refreshData()
        }

       viewModel.offeLiveData.observe(this, Observer {
           adapter.notifyDataSetChanged()
       })


        /* setup recycler view */
        binding.rvTrucks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeTrucksFragment.adapter
            addOnScrollListener(ButtonRVScrollListener())
            addOnScrollListener(PaginationInterface())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        binding.editStickySearch.clearFocus()
                        hideKeyboard(binding.editStickySearch)
                    }
                }
            })
        }


        binding.truckInventoryCardInner.btnAddTruckCard.setOnClickListener {
            binding.editStickySearch.clearFocus()
            hideKeyboard(binding.editStickySearch)
            
            when (viewModel.userPrefs.canBid()) {
                APPROVED -> {
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_ADD_TRUCK_INITIATE,
                            mutableListOf(PROPERTY_SOURCE),
                            mutableListOf(VALUE_MY_TRUCKS)
                    )
                    // Directly open add truck screen without bottom sheet
                    context?.let { startActivityForResult(truckIntent(requireContext(), source = VALUE_ADD_TRUCK_PAGE), REQCODE_ADD_TRUCK) }
                }
                UNAPPROVED -> {
                    dialogUtils.showBasicConfirmDialog(
                            string.title_dialog_supplier_not_approved,
                            string.msg_dialog_supplier_not_approved,
                            getString(string.label_call_us), getString(string.label_mail_us),
                            { callHelpline() }, { sendMail() }
                    )
                }
                DISABLED -> {
                    dialogUtils.showBasicConfirmDialog(
                            string.title_dialog_supplier_disabled,
                            string.msg_dialog_supplier_disabled,
                            getString(string.label_call_us), getString(string.label_mail_us),
                            { callHelpline() }, { sendMail() }
                    )
                }
            }
        }

        // Setup truck inventory card collapse/expand functionality
        setupTruckInventoryCard()

        binding.truckInventoryCardInner.btnBuyFastag.setOnClickListener {
            binding.editStickySearch.clearFocus()
            hideKeyboard(binding.editStickySearch)
            //showBuyFastagBottomSheet()
            startActivity(Intent(requireContext(), BuyFasTagActivity::class.java))

        }

        // Setup filter icon click listener
        binding.filterIcon.setOnClickListener {
            binding.editStickySearch.clearFocus()
            hideKeyboard(binding.editStickySearch)
            showFilterBottomSheet()
        }

        binding.editStickySearch.getTextChangeObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { it.isEmpty() || it.length >= 2 }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                if (query.isEmpty()) {
                    viewModel.searchPrefix = ""
                    viewModel.searchFlag = false
                    adapter.resetStaticData()
                    viewModel.getAllInventories(search = false)
                } else {
                    viewModel.searchPrefix = query
                    viewModel.searchFlag = true
                    adapter.resetStaticData()
                    viewModel.getAllInventories(search = true)
                }
            }.also { compositeDisposable.add(it) }



        /** Observe live Data*/

        viewModel.userTrucksData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items ->
                if (isFirstLoad) {
                    isFirstLoad = false
                    binding.initialLoader.root.visibility = View.GONE
                    binding.coordinatorLayout.visibility = View.VISIBLE
                }
                adapter.operation(_items)
                if (adapter.itemCount > 0 && userPrefs.isFirstOpenRate) {
                    userPrefs.isFirstOpenRate = false
                    bannerValue = true
                } else {
                    bannerValue = false
                }
                
                val hasError = _items.any { pair ->
                    pair.first is HomeTrucksWarningItem && 
                    (pair.first as HomeTrucksWarningItem).data.title.contains("timed out", ignoreCase = true)
                }
                
                if (hasError) {
                    binding.truckInventoryCardShimmer.root.visibility = View.GONE
                    binding.truckInventoryCardInner.root.visibility = View.GONE
                    binding.editStickySearch.visibility = View.GONE
                    binding.filterIcon.visibility = View.GONE
                } else {
                    binding.editStickySearch.visibility = View.VISIBLE
                    binding.filterIcon.visibility = View.VISIBLE
                }
            }
        })

        viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
            isLoadingData = it ?: false
            if (!isLoadingData && (HomeLoadsTruckFragment._instance.fromDeepLink || HomeLoadsTruckFragment._instance.fromNotification) && HomeLoadsTruckFragment._instance.vehicleNo.isNotEmpty()) {
                if (adapter.itemsList().size == 5)
                    this.handleAction(HomeTrucksRequestAction_ActivateTruck, adapter.itemsList().get(4), 4)
                HomeLoadsTruckFragment._instance.fromNotification = false
                HomeLoadsTruckFragment._instance.fromDeepLink = false
            }
        })

        viewModel.noCityCodeError.observe(this, Observer {
            if (it) {
                uiUtils.hideProgress()
                dialogUtils.showErrorDialog(
                        "City Code is missing",
                        3L
                )
            }
        })

        viewModel.activateTruckLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if (it != null) {
                if (it.first != -1 && it.first != -2) {
                    uiUtils.showSnackbar("Truck Activated Successfully")
                    val data = adapter.itemsList()[it.first].data as HomeTrucksRequestItemData
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_REQUEST_FOR_LOAD_SUBMIT,
                            mutableListOf(PROPERTY_INVENTORY_UUID),
                            mutableListOf(data.inventoryId ?: "")
                    )
                    data.ownership = it.second.ownership
                    data.latestStatus = it.second.latestStatus
                    data.latestUUID = it.second.latestUUID
                    data.currentCityName = it.second.currentCityName
                    data.currentCityCode = it.second.currentCityCode
                    data.unloadingDestination = it.second.unloadingDestination
                    data.unloadingDestinationCode = it.second.unloadingDestinationCode
                    data.unloadingDestinationAmount = it.second.unloadingDestinationAmount
                    data.unloadingDestinationRate = it.second.unloadingDestinationRate
                    data.originClusterId = it.second.originClusterId
                    data.destinationClusterId = it.second.destinationClusterId

                    adapter.notifyItemChanged(it.first)
                } else if (it.first == -2) {
                    dialogUtils.showErrorDialog(
                            "City is not mapped to cluster",
                            3L
                    )
                } else {
                    uiUtils.showSnackbar("Truck Activated Successfully")
                    refreshData()
                }
            }
        })

        viewModel.deactivateTruckLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if (it != null) {
                uiUtils.showSnackbar("Truck Deactivated Successfully")
                val data = adapter.itemsList()[it.first].data as HomeTrucksRequestItemData
                data.ownership = it.second.ownership
                data.latestStatus = it.second.latestStatus
                data.latestUUID = it.second.latestUUID
                data.currentCityName = it.second.currentCityName
                data.currentCityCode = it.second.currentCityCode
                data.unloadingDestination = it.second.unloadingDestination
                data.unloadingDestinationCode = it.second.unloadingDestinationCode
                data.unloadingDestinationAmount = it.second.unloadingDestinationAmount
                data.unloadingDestinationRate = it.second.unloadingDestinationRate
                data.originClusterId = it.second.originClusterId
                data.destinationClusterId = it.second.destinationClusterId
                adapter.notifyItemChanged(it.first)
            }
        })

        viewModel.editTruckLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if (it != null) {
                if (it.first == -2) {
                    dialogUtils.showErrorDialog(
                            "City is not mapped to cluster",
                            3L
                    )
                } else {

                    uiUtils.showSnackbar("Truck Edited Successfully")
                    val data = adapter.itemsList()[it.first].data as HomeTrucksRequestItemData
                    var fieldEdited = ""
                    if (data.ownership != it.second.ownership) {
                        if (!fieldEdited.isNullOrEmpty()) {
                            fieldEdited = fieldEdited + "/" + VALUE_OWNERSHIP
                        } else {
                            fieldEdited = VALUE_OWNERSHIP
                        }
                    }
                    if (data.currentCityName != it.second.currentCityName) {
                        if (!fieldEdited.isNullOrEmpty()) {
                            fieldEdited = fieldEdited + "/" + VALUE_ORIGIN
                        } else {
                            fieldEdited = VALUE_ORIGIN
                        }
                    }
                    if (data.unloadingDestination != it.second.unloadingDestination) {
                        if (!fieldEdited.isNullOrEmpty()) {
                            fieldEdited = fieldEdited + "/" + VALUE_DESTINATION
                        } else {
                            fieldEdited = VALUE_DESTINATION
                        }
                    }
                    if (data.unloadingDestinationAmount != it.second.unloadingDestinationAmount) {
                        if (!fieldEdited.isNullOrEmpty()) {
                            fieldEdited = fieldEdited + "/" + VALUE_PRICE
                        } else {
                            fieldEdited = VALUE_PRICE
                        }
                    }

                    data.ownership = it.second.ownership
                    data.latestStatus = it.second.latestStatus
                    data.latestUUID = it.second.latestUUID
                    data.currentCityName = it.second.currentCityName
                    data.currentCityCode = it.second.currentCityCode
                    data.unloadingDestination = it.second.unloadingDestination
                    data.unloadingDestinationCode = it.second.unloadingDestinationCode
                    data.unloadingDestinationAmount = it.second.unloadingDestinationAmount
                    data.unloadingDestinationRate = it.second.unloadingDestinationRate
                    data.originClusterId = it.second.originClusterId
                    data.destinationClusterId = it.second.destinationClusterId

                    adapter.notifyItemChanged(it.first)
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_EDIT_TRUCK_SUBMIT,
                            mutableListOf(PROPERTY_INVENTORY_UUID,
                                    PROPERTY_FIELD_EDITED),
                            mutableListOf(data.inventoryId ?: "", fieldEdited)
                    )
                }
            }
        })

        viewModel.deleteTruckLiveData.observe(this, Observer {
            uiUtils.showSnackbar("Truck Deleted Successfully")
            uiUtils.hideProgress()
            if (it != null) {
                adapter.itemsList().removeAt(it.first)
                adapter.notifyItemRemoved(it.first)
                adapter.notifyDataSetChanged()
            }
        })

        // Observe FASTag stats
        viewModel.fastagStatsData.observe(this, Observer { stats ->
            stats?.let {
                binding.truckInventoryCardShimmer.root.visibility = View.GONE
                binding.truckInventoryCardInner.root.visibility = View.VISIBLE

                binding.truckInventoryCardInner.tvTruckCount.text = "${it.totalTrucks} trucks in inventory"
                binding.truckInventoryCardInner.tvFastagCount.text = it.fastagTrucksCount.toString()
                binding.truckInventoryCardInner.tvFastagBalance.text = "₹${String.format("%,.0f", it.totalFastagBalance)}"

                sendAnalyticsEvent(stats)

            }
        })
        
        // Observe FASTag balance refresh
        viewModel.fastagBalanceRefreshData.observe(this, Observer { result ->
            result?.let { pair ->
                val tagId = pair.first
                val response = pair.second
                val newBalance = response.fastagBalance
                
                // Find the truck item with matching fastagTagId
                val items = adapter.itemsList()
                val position = items.indexOfFirst { item ->
                    if (item is HomeTrucksRequestItem) {
                        item.data.fastagTagId == tagId
                    } else {
                        false
                    }
                }
                
                if (position != -1) {
                    // Update the balance in the data model
                    val truckItem = items[position] as HomeTrucksRequestItem
                    truckItem.data.fastagBalance = newBalance
                    adapter.notifyItemChanged(position)

                    recalculateFastagStats()
                }
            }
        })
        
        // Observe FASTag balance refresh errors
        viewModel.fastagBalanceRefreshErrorData.observe(this, Observer { result ->
            result?.let { pair ->
                val tagId = pair.first
                val errorMessage = pair.second
                
                uiUtils.showSnackbar("Failed to refresh balance: $errorMessage")
            }
        })

        // Add tooltip on info icon click
        binding.truckInventoryCardInner.ivInfoIcon.setOnClickListener {
            showTooltip(it, "This balance is the total of all available FASTag balances")
        }

        if((HomeLoadsTruckFragment._instance.fromNotification||HomeLoadsTruckFragment._instance.fromDeepLink) && HomeLoadsTruckFragment._instance.vehicleNo.isNotEmpty()){
            analyticsUtil.moEngageTrackEvent(
                    EVENT_VIEW_MY_TRUCK,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                    mutableListOf(userPrefs.userId(), "trucks_screen")
            )
            viewModel.searchPrefix = HomeLoadsTruckFragment._instance.vehicleNo
            adapter.clearItems()
            viewModel.userTrucksData.postValue(null)
            viewModel.searchFlag = true
            if (isFirstLoad) {
                binding.coordinatorLayout.visibility = View.GONE
                binding.initialLoader.root.visibility = View.VISIBLE
            }
            viewModel.getAllInventories(search = true)
        }else{
            if(HomeLoadsTruckFragment._instance.fromNotification){
                analyticsUtil.moEngageTrackEvent(
                        EVENT_VIEW_MY_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME, PROPERTY_SOURCE),
                        mutableListOf(userPrefs.userId(), "trucks_screen", VALUE_NOTIFICATION)
                )
            }else if(HomeLoadsTruckFragment._instance.fromDeepLink){
                analyticsUtil.moEngageTrackEvent(
                        EVENT_VIEW_MY_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME, PROPERTY_SOURCE),
                        mutableListOf(userPrefs.userId(), "trucks_screen", VALUE_DEEP_LINKING)
                )
            }else{
                analyticsUtil.moEngageTrackEvent(
                        EVENT_VIEW_MY_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                        mutableListOf(userPrefs.userId(), "trucks_screen")
                )
            }
            HomeLoadsTruckFragment._instance.fromDeepLink = false
            HomeLoadsTruckFragment._instance.fromNotification = false
            HomeLoadsTruckFragment._instance.vehicleNo = ""
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
            return
        }
        binding.editStickySearch.clearFocus()
        updateFilterIndicator()
    }

    /**
     * Helper function to hide keyboard from a view
     */
    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun refreshData(filter: Boolean = false) {
        viewModel.paginateCount = 0
        if (isFirstLoad) {
            binding.coordinatorLayout.visibility = View.GONE
            binding.initialLoader.root.visibility = View.VISIBLE
            adapter.clearItems()
        } else {
            adapter.resetStaticData()
        }
        if(!filter) {
            if(!binding.editStickySearch.text.isNullOrEmpty())
              binding.editStickySearch.setText("")
            viewModel.searchPrefix = ""
            viewModel.searchFlag = false
            viewModel.bodyTypeFilter = mutableListOf()
            viewModel.sizeFilter = null
            viewModel.availabilityFilter = mutableListOf()
        }
        viewModel.getAllInventories()

        viewModel.offersLiveData.clear()
        updateFilterIndicator()
    }

    /**
     * Update filter indicator dot visibility based on active filters
     */
    private fun updateFilterIndicator() {
        val hasActiveFilters = viewModel.bodyTypeFilter.isNotEmpty() || 
                               viewModel.availabilityFilter.isNotEmpty() || 
                               viewModel.sizeFilter?.isNotEmpty() == true
        
        binding.filterIndicatorDot.visibility = if (hasActiveFilters) View.VISIBLE else View.GONE
    }

    private fun recalculateFastagStats() {
        val items = adapter.itemsList()
        val trucksList = items.filterIsInstance<HomeTrucksRequestItem>().map { it.data }
        
        val fastagTrucksCount = trucksList.count { 
            it.fastagTagStatus?.equals("Active", ignoreCase = true) == true 
        }
        val totalFastagBalance = trucksList
            .filter { it.fastagTagStatus?.equals("Active", ignoreCase = true) == true }
            .sumOf { it.fastagBalance?.toDoubleOrNull() ?: 0.0 }
        
        val stats = FastagStats(
            totalTrucks = viewModel.total,
            fastagTrucksCount = fastagTrucksCount,
            totalFastagBalance = totalFastagBalance
        )
        
        viewModel.fastagStatsData.postValue(stats)
    }

    override fun handleAction(actionId: String, item: BaseHomeTrucksRVAdapterItem<*>) {
        when(actionId){
            HomeTrucksVehicleFilterAction -> {
                showVehicleFilterDialog()
            }

            HomeTrucksAvailabilityFilterAction -> {
                showAvailabilityFilterDialog()
            }

            HomeTrucksSizeFilterAction -> {
                if (viewModel.bodyTypeFilter.isNotEmpty() && viewModel.truckSizeData.isNotEmpty()) {
                    showSizeFilterDialog()
                } else {
                    uiUtils.showSnackbar("Select Vehicle Type Filter First")
                }
            }

            HomeTrucksWarningAction_NoTrucks -> {
                context?.let { startActivityForResult(truckIntent(requireContext(), source = VALUE_ADD_TRUCK_PAGE), REQCODE_ADD_TRUCK) }
            }

            HomeTrucksTimeOutAction -> {
                refreshData()
            }
            
            HomeTrucksRequestAction_BuyFastag -> {
                val data = item.data as? HomeTrucksRequestItemData
                submitFastagLeadRequest(
                    vehicleCount = 1,
                    vrn = data?.vehicleNumber
                )
            }

            HomeTrucksPriorityAction -> {
                analyticsUtil.moEngageTrackEvent(
                        EVENT_BANNER_CLICK_TOP,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                        mutableListOf(userPrefs.userId(), "trucks_screen")
                )
                when (viewModel.userPrefs.canBid()) {
                    APPROVED -> {
                        analyticsUtil.moEngageTrackEvent(
                                EVENT_ADD_TRUCK_INITIATE,
                                mutableListOf(PROPERTY_SOURCE),
                                mutableListOf(VALUE_BANNER)
                        )
                        showAddTruckDialog(mutableListOf(TruckFrequentItem("closed", "32FTMXL", 14.0, 14.0, 18.0, "FTL"),
                                TruckFrequentItem("open", "10_TYRE", 16.0, 15.0, 20.0, "PMT"),
                                TruckFrequentItem("open", "12_TYRE", 21.0, 20.0, 25.0, "PMT")
                        ), VALUE_ADD_TRUCK_TOP_BANNER)
                    }
                    UNAPPROVED -> {
                        dialogUtils.showBasicConfirmDialog(
                                string.title_dialog_supplier_not_approved,
                                string.msg_dialog_supplier_not_approved,
                                getString(string.label_call_us), getString(string.label_mail_us),
                                { callHelpline() }, { sendMail() }
                        )
                    }
                    DISABLED -> {
                        dialogUtils.showBasicConfirmDialog(
                                string.title_dialog_supplier_disabled,
                                string.msg_dialog_supplier_disabled,
                                getString(string.label_call_us), getString(string.label_mail_us),
                                { callHelpline() }, { sendMail() }
                        )
                    }
                }

            }
        }
    }


    override fun handleAction(
            actionId: String,
            item: BaseHomeTrucksRVAdapterItem<*>,
            position: Int
    ) {
        //handle action here
        when(actionId){
            HomeTrucksRequestAction_EditTruck -> {
                showOptionsDialog(item.data as HomeTrucksRequestItemData, position)
            }

            HomeTrucksRequestAction_ActivateTruck -> {
                context?.let {
                    ActivateTruckDialog(requireContext(), item.data as HomeTrucksRequestItemData, viewModel, userPrefs, analyticsUtil, uiUtils, position, HomeLoadsTruckFragment._instance.fromDeepLink, HomeLoadsTruckFragment._instance.fromNotification).show()
                }
            }
            
            HomeTrucksRequestAction_BuyFastag -> {
                val data = item.data as? HomeTrucksRequestItemData
                submitFastagLeadRequest(
                    vehicleCount = 1,
                    vrn = data?.vehicleNumber
                )
            }
        }
    }


    private fun showAddTruckDialog(items: List<TruckFrequentItem>, source: String) {
        val dialog = Dialog(requireContext())
        val bindingDialog= DialogBottomTruckAddBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.containerTrucks.removeAllViews()
        items.forEachIndexed { index, item ->
            val itemBinding = createTruckFrequentItem(bindingDialog)
            itemBinding.data = item
            itemBinding.root.setOnClickListener{
                context?.let { startActivityForResult(truckIntent(requireContext(), item.truckType, item.truckSize, item.capacity, item.minCap, item.maxCap, item.sourcedAs, source = source), REQCODE_ADD_TRUCK) }
                dialog.dismiss()
            }

            bindingDialog.containerTrucks.addView(itemBinding.root, index)
        }
        bindingDialog.closeBtn.setOnClickListener{
            dialog.dismiss()
        }

        bindingDialog.addTruckLayout.setOnClickListener{
            context?.let { startActivityForResult(truckIntent(requireContext(), source = source), REQCODE_ADD_TRUCK) }
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    private fun showOptionsDialog(data: HomeTrucksRequestItemData, position: Int) {
        val dialog = Dialog(requireContext())
        val bindingDialog= DialogBottomTruckOptionsBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        if(data.latestStatus == "Free"){
            bindingDialog.deleteTruckLayout.visibility  = View.GONE}
        else{
            bindingDialog.deactivateTruckLayout.visibility = View.GONE
        }
        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.editTruckLayout.setOnClickListener {
            analyticsUtil.moEngageTrackEvent(
                    EVENT_EDIT_TRUCK_INITIATE,
                    mutableListOf(PROPERTY_INVENTORY_UUID),
                    mutableListOf(data.inventoryId ?: "")
            )
            context?.let {  EditTruckDialog(requireContext(), data, viewModel, userPrefs, analyticsUtil, uiUtils, position).show()}
            dialog.dismiss()
        }
        bindingDialog.deactivateTruckLayout.setOnClickListener {
            showDeactivateDialog(position, data)
            dialog.dismiss()
        }

        bindingDialog.deleteTruckLayout.setOnClickListener{
            uiUtils.showProgress()
            analyticsUtil.moEngageTrackEvent(
                    EVENT_DELETE_TRUCK,
                    mutableListOf(PROPERTY_INVENTORY_UUID),
                    mutableListOf(data.inventoryId ?: "")
            )
            analyticsUtil.moEngageTrackEvent(
                    EVENT_DELETE_TRUCK,
                    mutableListOf(PROPERTY_INVENTORY_ID),
                    mutableListOf(data.inventoryId ?: "")
            )
            viewModel.deleteTruck(data, position)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    private fun showDeactivateDialog(position: Int, data: HomeTrucksRequestItemData) {
        val dialog = Dialog(context!!)
        val bindingDialogDeactivate= DialogBottomTruckDeactivateBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialogDeactivate.root)

        bindingDialogDeactivate.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialogDeactivate.btnDeactivate.setOnClickListener {
            var reason: String = ""
            if (bindingDialogDeactivate.otherSource.isChecked){
                reason = bindingDialogDeactivate.otherSource.text.toString()
            }
             else if( bindingDialogDeactivate.other.isChecked) {
                 reason = bindingDialogDeactivate.other.text.toString()
            }

            if(reason != "") {
                analyticsUtil.moEngageTrackEvent(
                        EVENT_DEACTIVATE_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID, PROPERTY_REASON),
                        mutableListOf(userPrefs.userId(), data.inventoryId ?: "", reason)
                )
                uiUtils.showProgress()
                viewModel.deactivateTruck(data, reason, position)
                dialog.dismiss()
            }
            else{
                uiUtils.showSnackbar("Select Reason for deactivating truck")
            }
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showSizeFilterDialog() {
        lateinit var dialog: AlertDialog

        val currentVehicleFilterList = mutableListOf<String>()

        if (viewModel.bodyTypeFilter.isNotEmpty()) {
            for(item in viewModel.bodyTypeFilter){
                currentVehicleFilterList.add(item.second)
            }
        }

        val finalFilterList = ArrayList<String>()
        for(truck in viewModel.truckSizeData.sortedBy { it.defaultMG }){
            if(currentVehicleFilterList.contains(truck.truckType!!)){
                finalFilterList.add(truck.truckUuid!!)
            }
        }

        // Initialize an array of vehicles
        val arraySize = finalFilterList.toArray(arrayOfNulls<CharSequence>(finalFilterList.size));

        val arrayChecked  = BooleanArray(finalFilterList.size){false}

        val currentSizeFilterList = mutableListOf<String>()

        if (viewModel.sizeFilter.isNotNullOrEmpty()) {
            currentSizeFilterList.addAll(viewModel.sizeFilter!!.split(","))
        }

        if (currentSizeFilterList.isNotEmpty()) {
            for (item in currentSizeFilterList) {
                if (arraySize.contains(item))
                {
                    arrayChecked[arraySize.indexOf(item)] = true
                }
            }
        }

        val builder = AlertDialog.Builder(context, R.style.DatePickerTheme)

        builder.setTitle("-- Select Size --")

        builder.setMultiChoiceItems(arraySize, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

            var filterSizeTypes = listOf<String>()
            for (item in arraySize) {
                if (arrayChecked[arraySize.indexOf(item)]) {
                    filterSizeTypes  = filterSizeTypes + item.toString()
                }
            }
            viewModel.sizeFilter = filterSizeTypes.joinToString(separator = ",") {it}
            refreshData(true)

        }

        builder.setNegativeButton("Cancel") { _, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showAvailabilityFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayAvailable = arrayOf("Available", "Not Available")

        val arrayChecked = booleanArrayOf(false, false)

        val availableFilterList = mutableListOf<Pair<String, String>>()

        if (viewModel.availabilityFilter.isNotEmpty()) {
            availableFilterList.addAll(viewModel.availabilityFilter)
        }

        if (availableFilterList.isNotEmpty()) {
            for (item in availableFilterList) {
                if (arrayAvailable.contains(item.first))
                {
                    arrayChecked[arrayAvailable.indexOf(item.first)] = true
                }
            }
        }

        val builder = AlertDialog.Builder(context, R.style.DatePickerTheme)

        builder.setTitle("-- Select Availability --")

        builder.setMultiChoiceItems(arrayAvailable, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

            val filterAvailabilityTypes = mutableListOf<Pair<String, String>>()
            for (item in arrayAvailable) {
                if (arrayChecked[arrayAvailable.indexOf(item)]) {
                    when (item){
                        "Available" -> {
                            filterAvailabilityTypes.add(Pair(item, "Free"))
                        }
                        "Not Available" -> {
                            filterAvailabilityTypes.add(Pair(item, "not_available"))
                        }
                    }

                }
            }
            viewModel.availabilityFilter = filterAvailabilityTypes
            refreshData(true)

        }

        builder.setNegativeButton("Cancel") { _, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showVehicleFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayBody = arrayOf("Open", "Closed", "Trailer")

        val arrayChecked = booleanArrayOf(false, false, false)

        val currentVehicleFilterList = mutableListOf<Pair<String, String>>()

        if (viewModel.bodyTypeFilter.isNotEmpty()) {
            currentVehicleFilterList.addAll(viewModel.bodyTypeFilter)
        }

        if (currentVehicleFilterList.isNotEmpty()) {
            for (vehicle in currentVehicleFilterList) {
                if (arrayBody.contains(vehicle.first))
                {
                    arrayChecked[arrayBody.indexOf(vehicle.first)] = true
                }
            }
        }

        val builder = AlertDialog.Builder(context, R.style.DatePickerTheme)

        builder.setTitle("-- Select Vehicle Type --")

        builder.setMultiChoiceItems(arrayBody, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

            val filterBodyTypes =  mutableListOf<Pair<String, String>>()
            for (vehicle in arrayBody) {
                if (arrayChecked[arrayBody.indexOf(vehicle)]) {
                    when (vehicle){
                        "Open" -> {
                            filterBodyTypes.add(Pair(vehicle, "open"))
                        }
                        "Closed" -> {
                            filterBodyTypes.add(Pair(vehicle, "closed"))
                        }
                        "Trailer" -> {
                            filterBodyTypes.add(Pair(vehicle, "trailer"))
                        }
                    }
                }
            }
            viewModel.bodyTypeFilter = filterBodyTypes
            viewModel.sizeFilter =  null

            refreshData(true)

        }

        builder.setNegativeButton("Cancel") { _, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQCODE_ADD_TRUCK -> {
                if (data != null && data.getStringExtra("Added") == "Truck Added") {
                    refreshData()
                }
            }
        }
    }

    override fun getBannerStatus(): Boolean? {
        userPrefs.isFirstOpenRate = false
       return bannerValue
    }

    override fun callRewards() {
         analyticsUtil.moEngageTrackEvent(
                 EVENT_CLICKED_PRICE_BANNER,
                 mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO),
                 mutableListOf(userPrefs.userId(), userPrefs.phoneNumber ?: "")
         )
            navigationUtils.navigate(ShareRateGetRewardsActivity::class.java)
    }

    override fun gettotal(): Int {
       return totalTruck
    }

    override fun settotal(total: Int) {
       totalTruck = total
    }

    override fun callShareRate(data: HomeTrucksRequestItemData?, itemTD: String?, offerTD: String?, offerid: String?,amt:String?) {
        val bundle = Bundle()
        bundle.putString("originname", data?.currentCityName)
        bundle.putString("destname", data?.unloadingDestination)
        bundle.putString("occ", data?.currentCityCode)
        bundle.putString("dcc", data?.unloadingDestinationCode)
        bundle.putString("truckNumber", data?.vehicleNumber)
        bundle.putString("truckType", data?.truckSize)
        bundle.putString("truckCapacity", data?.truckCapacity())
        bundle.putString("itemTD", itemTD)
        bundle.putString("offerTD", offerTD)
        bundle.putString("offerid", offerid)
        bundle.putString("amt", amt)

        analyticsUtil.moEngageTrackEvent(
                EVENT_CLICKED_OFFER,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_SOURCE, PROPERTY_OFFER_ID),
                mutableListOf(userPrefs.userId(), userPrefs.phoneNumber
                        ?: "dummy", "inventory_screen", offerid ?: "")
        )

        navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
    }

    override fun getTotalOffers(data: HomeTrucksRequestItemData?) {
        Executors.newSingleThreadExecutor().execute(Runnable {
            viewModel.fetchDatabaseOffers(data)
        })
    }

    /**
     * Home trucks rv scroll listener for search bar animation related stuff
     */
    inner class HomeTrucksRVScrollListener(
            private val stickyView: View,
            private val elevation: Float = 12f
    ) : RecyclerView.OnScrollListener() {
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
//                stickyView.setRatio((1 - factor))
                defToolbarElevation
            } else {
                stickyView.translationY = 0f
                stickyView.alpha = 1f
//                stickyView.setRatio(0f)
                0f
            }
            if (toolbarElevation != this.toolbarElevation && toolbarElevationLiveData != null) {
                this.toolbarElevation = toolbarElevation
                toolbarElevationLiveData?.postValue(this.toolbarElevation)
            }
        }
    }

    inner class ButtonRVScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
        ) {
            super.onScrolled(recyclerView, dx, dy)

            if (visible && scrollDist > 0) {
                scrollDist = 0
                visible = false
            } else if (!visible && scrollDist < 0) {
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
        override fun loadMore() = viewModel.getAllInventories(true)

        override fun hasMore() = viewModel.hasMoreData

        override fun isLoading() = isLoadingData
    }

    /** Create new frequent truck item*/
    private fun createTruckFrequentItem(binding: DialogBottomTruckAddBinding)=
        ViewFrequentTruckItemBinding.inflate(layoutInflater, binding.containerTrucks, false)

    /**
     * Setup truck inventory card collapse/expand functionality
     */
    private fun setupTruckInventoryCard() {
        var isExpanded = true

        // Set click listener on header using data binding
        binding.truckInventoryCardInner.headerLayout.setOnClickListener {
            isExpanded = !isExpanded
            
            if (isExpanded) {
                // Expanded state - show collapse icon (up arrow)
                binding.truckInventoryCardInner.expandableContent.visibility = View.VISIBLE
                binding.truckInventoryCardInner.divider.visibility = View.VISIBLE
                binding.truckInventoryCardInner.ivCollapseIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                // Collapsed state - show expand icon (down arrow)
                binding.truckInventoryCardInner.expandableContent.visibility = View.GONE
                binding.truckInventoryCardInner.divider.visibility = View.GONE
                binding.truckInventoryCardInner.ivCollapseIcon.setImageResource(R.drawable.ic_expand)
            }
        }

    }

    /**
     * Show Buy FASTag bottom sheet
     */
    private fun showBuyFastagBottomSheet() {
        val dialog = BuyFastagBottomSheetDialogFragment.newInstance(
            maxTrucks = viewModel.total
        ) { selectedCity, truckCount ->
            submitFastagLeadRequest(
                vehicleCount = truckCount,
                location = "${selectedCity.cityName()},${selectedCity.state}",
                vrn = null
            )
        }

        dialog.show(parentFragmentManager, "BuyFastagBottomSheet")
    }

    /**
     * Show FASTag success bottom sheet
     */
    private fun showFastagSuccessBottomSheet() {
        val successDialog = FastagSuccessBottomSheetDialogFragment.newInstance()
        successDialog.show(parentFragmentManager, "FastagSuccessBottomSheet")
    }
    
    /**
     * Generic function to submit FASTag lead request
     * Can be called from anywhere without showing bottom sheet
     */
    private fun submitFastagLeadRequest(
        vehicleCount: Int = 1,
        location: String? = null,
        vrn: String? = null
    ) {
        uiUtils.showProgress()
        
        viewModel.submitFastagLead(
            vehicleCount = vehicleCount,
            location = location,
            vrn = vrn,
            onSuccess = { message ->
                uiUtils.hideProgress()
                showFastagSuccessBottomSheet()
            },
            onError = { errorMessage ->
                uiUtils.hideProgress()
                uiUtils.showSnackbar("Failed to submit request: $errorMessage")
            }
        )
    }

    /**
     * Show filter bottom sheet with all filter options
     */
    private fun showFilterBottomSheet() {
        val dialog = Dialog(requireContext())
        val bindingDialog = DialogBottomTruckFilterBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        // Track current tab
        var currentTab = "vehicle_type"
        
        // Store initial filter state to detect changes
        val initialVehicleTypeFilter = viewModel.bodyTypeFilter.toList()
        val initialAvailabilityFilter = viewModel.availabilityFilter.toList()
        val initialSizeFilter = viewModel.sizeFilter
        
        // Function to check if filters have changed from initial state
        fun hasFiltersChanged(): Boolean {
            // Get current selected filters from UI
            val currentVehicleTypes = mutableListOf<Pair<String, String>>()
            if (bindingDialog.cbOpenTruck.isChecked) currentVehicleTypes.add(Pair("Open", "open"))
            if (bindingDialog.cbClosedTruck.isChecked) currentVehicleTypes.add(Pair("Closed", "closed"))
            if (bindingDialog.cbTrailer.isChecked) currentVehicleTypes.add(Pair("Trailer", "trailer"))
            
            val currentAvailability = mutableListOf<Pair<String, String>>()
            if (bindingDialog.cbAvailable.isChecked) currentAvailability.add(Pair("Available", "Free"))
            if (bindingDialog.cbNotAvailable.isChecked) currentAvailability.add(Pair("Not Available", "not_available"))
            
            // Get current truck size selections
            val parentLayout = bindingDialog.llFilterContent
            val currentTruckSizeUuids = mutableListOf<String>()
            if (parentLayout != null) {
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null && child.isChecked) {
                        currentTruckSizeUuids.add(child.tag.toString())
                    }
                }
            }
            val currentSizeFilter = if (currentTruckSizeUuids.isNotEmpty()) {
                currentTruckSizeUuids.sorted().joinToString(",")
            } else null
            
            // Compare with initial state
            val vehicleTypeChanged = currentVehicleTypes.map { it.second }.sorted() != 
                                    initialVehicleTypeFilter.map { it.second }.sorted()
            val availabilityChanged = currentAvailability.map { it.second }.sorted() != 
                                     initialAvailabilityFilter.map { it.second }.sorted()
            val sizeFilterChanged = currentSizeFilter != initialSizeFilter
            
            return vehicleTypeChanged || availabilityChanged || sizeFilterChanged
        }
        
        // Function to check if any filter is selected and update Apply button state
        fun updateApplyButtonState() {
            val hasVehicleTypeFilter = bindingDialog.cbOpenTruck.isChecked || 
                                       bindingDialog.cbClosedTruck.isChecked || 
                                       bindingDialog.cbTrailer.isChecked
            
            val hasAvailabilityFilter = bindingDialog.cbAvailable.isChecked || 
                                        bindingDialog.cbNotAvailable.isChecked
            
            // Check dynamic truck size checkboxes
            var hasTruckSizeFilter = false
            val parentLayout = bindingDialog.llFilterContent
            if (parentLayout != null) {
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null && child.isChecked) {
                        hasTruckSizeFilter = true
                        break
                    }
                }
            }
            
            val hasAnyFilter = hasVehicleTypeFilter || hasAvailabilityFilter || hasTruckSizeFilter
            val hasChanged = hasFiltersChanged()
            
            // Enable Apply button only if there are filters AND they have changed
            bindingDialog.btnApply.isEnabled = hasAnyFilter && hasChanged
            bindingDialog.btnApply.alpha = if (hasAnyFilter && hasChanged) 1.0f else 0.5f
        }

        // Function to update tab UI
        fun updateTabUI(selectedTab: String) {
            // Reset all tabs to default state
            bindingDialog.tabVehicleType.setBackgroundColor(resources.getColor(android.R.color.transparent))
            bindingDialog.tabVehicleType.setTextColor(resources.getColor(R.color.text_grey))
            bindingDialog.tabAvailability.setBackgroundColor(resources.getColor(android.R.color.transparent))
            bindingDialog.tabAvailability.setTextColor(resources.getColor(R.color.text_grey))
            bindingDialog.tabTruckSize.setBackgroundColor(resources.getColor(android.R.color.transparent))
            bindingDialog.tabTruckSize.setTextColor(resources.getColor(R.color.text_grey))

            bindingDialog.cbOpenTruck.visibility = View.GONE
            bindingDialog.cbClosedTruck.visibility = View.GONE
            bindingDialog.cbTrailer.visibility = View.GONE
            bindingDialog.cbAvailable.visibility = View.GONE
            bindingDialog.cbNotAvailable.visibility = View.GONE
            bindingDialog.tvEmptyMessage.visibility = View.GONE
            bindingDialog.etSearch.visibility = View.GONE
            
            val parentLayout = bindingDialog.llFilterContent
            if (parentLayout != null) {
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null) {
                        child.visibility = View.GONE
                    } else if (child is TextView && child.id != R.id.tvFilterTitle && 
                              (child.text.toString() == getString(R.string.msg_no_truck_sizes_available) ||
                               child.text.toString().contains("No truck sizes match"))) {
                        child.visibility = View.GONE
                    }
                }
            }

            when (selectedTab) {
                "vehicle_type" -> {
                    bindingDialog.tabVehicleType.setBackgroundColor(resources.getColor(android.R.color.white))
                    bindingDialog.tabVehicleType.setTextColor(resources.getColor(R.color.black))
                    bindingDialog.tvFilterTitle.text = getString(R.string.filter_by_vehicle_type)
                    bindingDialog.cbOpenTruck.visibility = View.VISIBLE
                    bindingDialog.cbClosedTruck.visibility = View.VISIBLE
                    bindingDialog.cbTrailer.visibility = View.VISIBLE
                }
                "availability" -> {
                    bindingDialog.tabAvailability.setBackgroundColor(resources.getColor(android.R.color.white))
                    bindingDialog.tabAvailability.setTextColor(resources.getColor(R.color.black))
                    bindingDialog.tvFilterTitle.text = getString(R.string.filter_by_availability)
                    bindingDialog.cbAvailable.visibility = View.VISIBLE
                    bindingDialog.cbNotAvailable.visibility = View.VISIBLE
                }
                "truck_size" -> {
                    bindingDialog.tabTruckSize.setBackgroundColor(resources.getColor(android.R.color.white))
                    bindingDialog.tabTruckSize.setTextColor(resources.getColor(R.color.black))
                    bindingDialog.tvFilterTitle.text = getString(R.string.filter_by_truck_size)
                }
            }
        }

        fun setupTabClickListener(
            tab: View,
            tabName: String,
            bindingDialogRef: DialogBottomTruckFilterBinding,
            onTabSelected: ((DialogBottomTruckFilterBinding) -> Unit)? = null
        ) {
            tab.setOnClickListener {
                hideKeyboard(bindingDialog.etSearch)
                bindingDialog.etSearch.clearFocus()
                
                currentTab = tabName
                updateTabUI(currentTab)
                
                // Invoke custom logic if any
                onTabSelected?.invoke(bindingDialog)
            }
        }

        // Tab click listeners
        setupTabClickListener(bindingDialog.tabVehicleType, "vehicle_type", bindingDialog)
        setupTabClickListener(bindingDialog.tabAvailability, "availability", bindingDialog)
        
        setupTabClickListener(bindingDialog.tabTruckSize, "truck_size", bindingDialog) {
            onTruckSizeTabSelected(it, ::updateApplyButtonState)
        }
        
        // Search functionality for truck size
        bindingDialog.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s?.toString()?.trim()?.lowercase() ?: ""
                
                // Only filter when on truck size tab
                if (currentTab == "truck_size") {
                    filterTruckSizeCheckboxes(bindingDialog, searchQuery)
                }
            }
        })

        // Close button
        bindingDialog.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        // Clear Filters button
        bindingDialog.btnClear.setOnClickListener {
            // Check if any filters are currently applied
            val hasFilters = viewModel.bodyTypeFilter.isNotEmpty() || 
                           viewModel.sizeFilter != null || 
                           viewModel.availabilityFilter.isNotEmpty()
            
            if (!hasFilters) {
                // No filters to clear, just dismiss the dialog
                dialog.dismiss()
                return@setOnClickListener
            }
            
            // Reset all filters
            viewModel.bodyTypeFilter = mutableListOf()
            viewModel.sizeFilter = null
            viewModel.availabilityFilter = mutableListOf()
            
            // Clear search field
            bindingDialog.etSearch.setText("")
            
            // Reset all checkboxes
            bindingDialog.cbOpenTruck.isChecked = false
            bindingDialog.cbClosedTruck.isChecked = false
            bindingDialog.cbTrailer.isChecked = false
            bindingDialog.cbAvailable.isChecked = false
            bindingDialog.cbNotAvailable.isChecked = false

            
            // Clear dynamically generated truck size checkboxes
            val parentLayout = bindingDialog.llFilterContent
            
            if (parentLayout != null) {
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null) {
                        child.isChecked = false
                    }
                }
            }
            
            refreshData(filter = true)
            dialog.dismiss()
            uiUtils.showSnackbar("Filters cleared")
        }

        // Apply button
        bindingDialog.btnApply.setOnClickListener {
            // Collect vehicle type filters
            val selectedVehicleFilters = mutableListOf<Pair<String, String>>()
            if (bindingDialog.cbOpenTruck.isChecked) {
                selectedVehicleFilters.add(Pair("Open", "open"))  // Fixed: Display name first, API value second
            }
            if (bindingDialog.cbClosedTruck.isChecked) {
                selectedVehicleFilters.add(Pair("Closed", "closed"))
            }
            if (bindingDialog.cbTrailer.isChecked) {
                selectedVehicleFilters.add(Pair("Trailer", "trailer"))
            }
            viewModel.bodyTypeFilter = selectedVehicleFilters

            // Collect availability filters
            val selectedAvailabilityFilters = mutableListOf<Pair<String, String>>()
            if (bindingDialog.cbAvailable.isChecked) {
                selectedAvailabilityFilters.add(Pair("Available", "Free"))
            }
            if (bindingDialog.cbNotAvailable.isChecked) {
                selectedAvailabilityFilters.add(Pair("Not Available", "not_available"))
            }

            viewModel.availabilityFilter = selectedAvailabilityFilters
            
            // Collect truck size filters (dynamically generated checkboxes)
            val parentLayout = bindingDialog.llFilterContent
            
            if (parentLayout != null) {
                val selectedTruckUuids = mutableListOf<String>()
                // Iterate through all children to find CheckBox views with tags (truck UUIDs)
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null && child.isChecked) {
                        selectedTruckUuids.add(child.tag.toString())
                    }
                }
                viewModel.sizeFilter = if (selectedTruckUuids.isNotEmpty()) {
                    selectedTruckUuids.joinToString(",")
                } else {
                    null
                }
            }
            
            // Refresh data with filters
            refreshData(filter = true)
            
            dialog.dismiss()
            uiUtils.showSnackbar("Filters applied")
        }

        // Set initial checkbox states based on current filters
        if (viewModel.bodyTypeFilter.isNotEmpty()) {
            for (filter in viewModel.bodyTypeFilter) {
                when (filter.first) {  // Fixed: Check display name (first), not API value
                    "Open" -> bindingDialog.cbOpenTruck.isChecked = true
                    "Closed" -> bindingDialog.cbClosedTruck.isChecked = true
                    "Trailer" -> bindingDialog.cbTrailer.isChecked = true
                }
            }
        }

        if (viewModel.availabilityFilter.isNotEmpty()) {
            for (filter in viewModel.availabilityFilter) {
                when (filter.first) {
                    "Available" -> bindingDialog.cbAvailable.isChecked = true
                    "Not Available" -> bindingDialog.cbNotAvailable.isChecked = true

                }
            }
        }
        
        bindingDialog.cbOpenTruck.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
        bindingDialog.cbClosedTruck.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
        bindingDialog.cbTrailer.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
        bindingDialog.cbAvailable.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
        bindingDialog.cbNotAvailable.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
        
        val parentLayout = bindingDialog.llFilterContent
        if (parentLayout != null) {
            for (i in 0 until parentLayout.childCount) {
                val child = parentLayout.getChildAt(i)
                if (child is CheckBox && child.tag != null) {
                    child.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
                }
            }
        }

        // Initialize with vehicle type tab selected
        updateTabUI(currentTab)
        
        // Hide search bar initially since Vehicle Type is the default tab
        bindingDialog.etSearch.visibility = View.GONE
        
        updateApplyButtonState()

        dialog.show()
        
        // Set dialog height to 75% of screen height
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val dialogHeight = (screenHeight * 0.75).toInt()
        
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }



    private fun onTruckSizeTabSelected(
        bindingDialog: DialogBottomTruckFilterBinding,
        updateApplyButtonState: (() -> Unit)? = null
    ) {
        // Check if vehicle type is selected
        if (viewModel.bodyTypeFilter.isEmpty()) {
            bindingDialog.tvEmptyMessage.visibility = View.VISIBLE
            return
        }
        
        // Check if truck size data is available
        if (viewModel.truckSizeData.isEmpty()) {
            return
        }
        
        val parentLayout = bindingDialog.llFilterContent
        
        // Check if checkboxes already exist
        var hasExistingCheckboxes = false
        if (parentLayout != null) {
            for (i in 0 until parentLayout.childCount) {
                val child = parentLayout.getChildAt(i)
                if (child is CheckBox && child.tag != null) {
                    hasExistingCheckboxes = true
                    child.visibility = View.VISIBLE
                }
                // Show search message if it exists
                if (child is TextView && child.text.toString().contains("No truck sizes match")) {
                    child.visibility = View.VISIBLE
                }
            }
        }
        
        // Generate checkboxes if they don't exist
        if (!hasExistingCheckboxes) {
            generateTruckSizeCheckboxes(bindingDialog)
            
            // Attach listeners to newly created checkboxes
            if (parentLayout != null && updateApplyButtonState != null) {
                for (i in 0 until parentLayout.childCount) {
                    val child = parentLayout.getChildAt(i)
                    if (child is CheckBox && child.tag != null) {
                        child.setOnCheckedChangeListener { _, _ -> updateApplyButtonState() }
                    }
                }
            }
        }
        
        // Show search bar
        bindingDialog.etSearch.visibility = View.VISIBLE
        
        val searchQuery = bindingDialog.etSearch.text.toString().trim().lowercase()
        if (searchQuery.isNotEmpty()) {
            filterTruckSizeCheckboxes(bindingDialog, searchQuery)
        }
    }

    /**
     * Generate truck size checkboxes dynamically based on selected vehicle types
     */
    private fun generateTruckSizeCheckboxes(bindingDialog: DialogBottomTruckFilterBinding) {
        // Get the contentContainer LinearLayout, then get the ScrollView (3rd child at index 2)
        val parentLayout = bindingDialog.llFilterContent
        
        if (parentLayout == null) {
            uiUtils.showSnackbar("Error loading truck sizes")
            return
        }
        
        // Remove only dynamically added checkboxes (those with tags), not static ones
        val childrenToRemove = mutableListOf<View>()
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child is CheckBox && child.tag != null) {
                childrenToRemove.add(child)
            } else if (child is TextView && child.text.toString() == getString(R.string.msg_no_truck_sizes_available)) {
                childrenToRemove.add(child)
            } else if (child is TextView && child.text.toString().contains("No truck sizes match")) {
                childrenToRemove.add(child)
            }
        }
        for (child in childrenToRemove) {
            parentLayout.removeView(child)
        }
        
        // Get selected vehicle types (API values)
        val selectedVehicleTypes = mutableListOf<String>()
        for (filter in viewModel.bodyTypeFilter) {
            selectedVehicleTypes.add(filter.second)
        }
        
        // Filter truck sizes based on selected vehicle types
        val afterFilter = viewModel.truckSizeData
            .filter { selectedVehicleTypes.contains(it.truckType) }
        val filteredTruckSizes = afterFilter
            .distinctBy { it.truckDisplayName }
            .sortedBy { it.defaultMG }
        
        if (filteredTruckSizes.isEmpty()) {
            // Show message if no truck sizes available
            val textView = TextView(requireContext()).apply {
                text = getString(R.string.msg_no_truck_sizes_available)
                setTextColor(resources.getColor(R.color.text_grey))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 20
                }
            }
            parentLayout.addView(textView)
            return
        }
        
        // Get currently selected truck size UUIDs
        val selectedTruckUuids = if (viewModel.sizeFilter.isNotNullOrEmpty()) {
            viewModel.sizeFilter!!.split(",").toMutableList()
        } else {
            mutableListOf()
        }
        
        // Create checkboxes for each truck size
        for (truck in filteredTruckSizes) {
            val checkbox = CheckBox(requireContext()).apply {
                text = truck.truckDisplayName ?: truck.truckUuid
                setTextColor(resources.getColor(R.color.black))
                textSize = 14f
                buttonTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.black))
                tag = truck.truckUuid // Store UUID in tag for later retrieval
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (filteredTruckSizes.indexOf(truck) == 0) 20 else 12
                }
            }
            checkbox.isChecked = selectedTruckUuids.contains(truck.truckUuid)
            parentLayout.addView(checkbox)
        }
    }

    /**
     * Filter truck size checkboxes based on search query
     */
    private fun filterTruckSizeCheckboxes(bindingDialog: DialogBottomTruckFilterBinding, searchQuery: String) {
        val parentLayout = bindingDialog.llFilterContent
        
        if (parentLayout == null) return
        
        var visibleCount = 0
        
        // Filter checkboxes based on search query
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child is CheckBox && child.tag != null) {
                val checkboxText = child.text.toString().lowercase()
                if (searchQuery.isEmpty() || checkboxText.contains(searchQuery)) {
                    child.visibility = View.VISIBLE
                    visibleCount++
                } else {
                    child.visibility = View.GONE
                }
            }
        }
        
        // Show/hide "No results" message
        var noResultsTextView: TextView? = null
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child is TextView && child.text.toString().contains("No truck sizes match")) {
                noResultsTextView = child
                break
            }
        }
        
        if (visibleCount == 0 && searchQuery.isNotEmpty()) {
            // Show "No results" message if not already present
            if (noResultsTextView == null) {
                noResultsTextView = TextView(requireContext()).apply {
                    text = "No truck sizes match your search"
                    setTextColor(resources.getColor(R.color.text_grey))
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 20
                    }
                }
                parentLayout.addView(noResultsTextView)
            } else {
                noResultsTextView.visibility = View.VISIBLE
            }
        } else {
            // Hide "No results" message
            noResultsTextView?.visibility = View.GONE
        }
    }

    private fun showTooltip(anchorView: View, message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val bindingTooltip = DialogTooltipBinding.inflate(layoutInflater)
        val tooltipView = bindingTooltip.root
        dialog.setContentView(tooltipView)
        
        val tvMessage = bindingTooltip.tvTooltipMessage
        tvMessage.text = message
        
        // Measure the tooltip view to get its dimensions
        tooltipView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val tooltipHeight = tooltipView.measuredHeight
        
        // Get the info icon location on screen
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val iconX = location[0]
        val iconY = location[1]
        val iconWidth = anchorView.width
        
        // Calculate tooltip position
        // Position tooltip slightly above and left-aligned with the icon
        val tooltipX = iconX - 35 // Move slightly left
        val tooltipY = iconY - tooltipHeight - 65 // Move slightly higher above the icon
        
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Remove dim background
            clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes.gravity = Gravity.TOP or Gravity.START
            attributes.x = tooltipX
            attributes.y = tooltipY
            attributes.width = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        
        dialog.show()
        
        // Auto dismiss after 3 seconds
        android.os.Handler().postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
        
        // Dismiss on click anywhere
        tooltipView.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun refreshFastagBalance(tagId: String) {
        uiUtils.showProgress()
        viewModel.refreshFastagBalance(tagId)
        
        // Hide progress after a short delay (will be hidden when response comes)sp
        android.os.Handler().postDelayed({
            uiUtils.hideProgress()
        }, 2000)
    }
    
    override fun openFastagDetails(data: HomeTrucksRequestItemData) {
        val intent = Intent(requireContext(), com.delhivery.axle.ui.fastag.FastagTransactionDetailsActivity::class.java).apply {
            putExtra(com.delhivery.axle.ui.fastag.FastagTransactionDetailsActivity.VEHICLE_DATA, data)
        }
        startActivity(intent)
    }

    override fun openFastagRecharge(data: HomeTrucksRequestItemData) {
        val intent = Intent(requireContext(), com.delhivery.axle.ui.fastag.FastagRechargeActivity::class.java).apply {
            putExtra(com.delhivery.axle.ui.fastag.FastagRechargeActivity.TAG_ID, data.fastagTagId)
            putExtra(com.delhivery.axle.ui.fastag.FastagRechargeActivity.VEHICLE_NUMBER, data.vehicleNumber)
            putExtra(com.delhivery.axle.ui.fastag.FastagRechargeActivity.FASTAG_BALANCE, data.fastagBalance ?: "0")
        }
        startActivityForResult(intent, REQCODE_FASTAG_RECHARGE)
    }

}

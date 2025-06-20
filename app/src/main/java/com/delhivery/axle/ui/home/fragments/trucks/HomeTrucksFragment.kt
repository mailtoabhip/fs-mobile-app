package com.delhivery.axle.ui.home.fragments.trucks

import android.annotation.SuppressLint
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
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.trucks.*
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.home.activity.home.OFF_SET_LIMIT
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.ui.trucks.ActivateTruckDialog
import com.delhivery.axle.ui.trucks.EditTruckDialog
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.concurrent.Executors
import javax.inject.Inject


class HomeTrucksFragment : HomeLoadsTruckBaseFragment<FragmentHomeTrucksBinding, HomeTrucksViewModel>(),
        HomeTrucksRVAdapterInterface
{
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeTrucksFragment_SetupTime")
        fragmentSetupTrace?.start()
        viewModel.fetchData()

        viewModel.fetchTruckType()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

       viewModel.offeLiveData.observe(this, Observer {
           adapter.notifyDataSetChanged()
       })


        /* setup recycler view */
        binding.rvTrucks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeTrucksFragment.adapter
            addOnScrollListener(HomeTrucksRVScrollListener(binding.editStickySearch))
            addOnScrollListener(ButtonRVScrollListener())
            addOnScrollListener(PaginationInterface())
        }

        binding.addTruck.setOnClickListener {
            when (viewModel.userPrefs.canBid()) {
                APPROVED -> {
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_ADD_TRUCK_INITIATE,
                            mutableListOf(PROPERTY_SOURCE),
                            mutableListOf(VALUE_MY_TRUCKS)
                    )
                    showAddTruckDialog(mutableListOf(TruckFrequentItem("closed", "32FTMXL", 14.0, 14.0, 18.0, "FTL"),
                            TruckFrequentItem("open", "10_TYRE", 16.0, 15.0, 20.0, "PMT"),
                            TruckFrequentItem("open", "12_TYRE", 21.0, 20.0, 25.0, "PMT")
                    ), VALUE_ADD_TRUCK_PAGE)
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
        binding.addTruckFloating.setOnClickListener {
            when (viewModel.userPrefs.canBid()) {
                APPROVED -> {
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_ADD_TRUCK_INITIATE,
                            mutableListOf(PROPERTY_SOURCE),
                            mutableListOf(VALUE_MY_TRUCKS)
                    )
                    showAddTruckDialog(mutableListOf(TruckFrequentItem("closed", "32FTMXL", 14.0, 14.0, 18.0, "FTL"),
                            TruckFrequentItem("open", "10_TYRE", 16.0, 15.0, 20.0, "PMT"),
                            TruckFrequentItem("open", "12_TYRE", 21.0, 20.0, 25.0, "PMT")
                    ), VALUE_ADD_TRUCK_PAGE)
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

        binding.editStickySearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
            ) = Unit

            override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
            ) {
                if (s != null && s.toString() != "") {
                    viewModel.searchPrefix = s.trim().toString()
                    if (viewModel.searchPrefix.length >= 2) {
                        adapter.clearItems()
                        viewModel.userTrucksData.postValue(null)
                        viewModel.searchFlag = true
                        viewModel.getAllInventories(search = true)
                    }

                }else{
                    adapter.clearItems()
                    viewModel.userTrucksData.postValue(null)
                    viewModel.searchFlag = false
                    viewModel.getAllInventories(search = false)
                }
            }
        })



        /** Observe live Data*/

        viewModel.userTrucksData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items ->
                adapter.operation(_items)
                if (adapter.itemCount > 0 && userPrefs.isFirstOpenRate) {
                    userPrefs.isFirstOpenRate = false
                    bannerValue = true
                } else {
                    bannerValue = false
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
                            mutableListOf(data.inventoryId, fieldEdited)
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
        }
    }

    private fun refreshData(filter: Boolean = false) {
        binding.addTruck.visibility = View.VISIBLE
        viewModel.paginateCount = 0
        adapter.resetStaticData()
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
                binding.addTruck.visibility = View.GONE
                refreshData()
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
                    mutableListOf(data.inventoryId)
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
                    mutableListOf(data.inventoryId)
            )
            analyticsUtil.moEngageTrackEvent(
                    EVENT_DELETE_TRUCK,
                    mutableListOf(PROPERTY_INVENTORY_ID),
                    mutableListOf(data.inventoryId)
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
                        mutableListOf(userPrefs.userId(), data.inventoryId, reason)
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

        val builder = AlertDialog.Builder(context)

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
        val arrayAvailable = arrayOf("Available", "Not Available", "Active")

        val arrayChecked = booleanArrayOf(false, false, false)

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

        val builder = AlertDialog.Builder(context)

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
                        "Active" -> {
                            filterAvailabilityTypes.add(Pair(item, "Active"))
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

        val builder = AlertDialog.Builder(context)

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

    @SuppressLint("RestrictedApi")
    fun hide() {
        binding.addTruck.visibility = View.GONE
        binding.addTruckFloating.visibility = View.VISIBLE
    }

    @SuppressLint("RestrictedApi")
    fun show() {
        binding.addTruck.visibility = View.VISIBLE
        binding.addTruckFloating.visibility = View.GONE
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
            private val stickyView: DelhiveryAnimatedSearchBar,
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

    inner class ButtonRVScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
        ) {
            super.onScrolled(recyclerView, dx, dy)

            if (visible && scrollDist > 0) {
                hide()
                scrollDist = 0
                visible = false
            } else if (!visible && scrollDist < 0) {
                show()
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

}

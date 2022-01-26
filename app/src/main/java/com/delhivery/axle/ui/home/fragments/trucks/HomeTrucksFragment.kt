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
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsPriorityAction
import com.delhivery.axle.data.home.trucks.*
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.trucks.ActivateTruckDialog
import com.delhivery.axle.ui.trucks.EditTruckDialog
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeTrucksFragment : HomeLoadsTruckBaseFragment<FragmentHomeTrucksBinding, HomeTrucksViewModel>(),
        HomeTrucksRVAdapterInterface
{
    override fun getViewModelClass() = HomeTrucksViewModel::class.java
    override fun layoutId() = R.layout.fragment_home_trucks

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchTruckType()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        /* setup recycler view */
        binding.rvTrucks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeTrucksFragment.adapter
            addOnScrollListener(HomeTrucksRVScrollListener(binding.editStickySearch))
        }

        binding.addTruck.setOnClickListener {
            showAddTruckDialog(mutableListOf(TruckFrequentItem("closed","32FTMXL",14.0,14.0,18.0, "FTL"),
                TruckFrequentItem("open","10_TYRE",16.0,15.0,20.0,"PMT"),
                TruckFrequentItem("open","12_TYRE",21.0,20.0,25.0,"PMT")
            ))
          //  context?.let {  EditTruckDialog(context!!, viewModel, userPrefs, analyticsUtil, uiUtils,1).show()}
        }
        binding.addTruckFloating.setOnClickListener {
            showAddTruckDialog(mutableListOf(TruckFrequentItem("closed","32FTMXL",14.0,14.0,18.0,"FTL"),
                TruckFrequentItem("open","10_TYRE",16.0,15.0,20.0,"PMT"),
                TruckFrequentItem("open","12_TYRE",21.0,20.0,25.0,"PMT")
            ))

        }

        /** Observe live Data*/

        viewModel.userTrucksData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })

        viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
            isLoadingData = it ?: false
        })

        viewModel.activateTruckLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if(it!=null){
                uiUtils.showSnackbar("Truck Activated Successfully")
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

        viewModel.deactivateTruckLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if(it!=null){
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
            if(it!=null){
                uiUtils.showSnackbar("Truck Edited Successfully")
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

        viewModel.deleteTruckLiveData.observe(this, Observer {
            uiUtils.showSnackbar("Truck Deleted Successfully")
            uiUtils.hideProgress()
            if(it!=null){
                adapter.notifyItemRemoved(it.first)
            }
        })

        refreshData()
    }


    private fun refreshData() {
        adapter.resetStaticData()
        viewModel.getAllInventories()

    }

    override fun handleAction(actionId: String, item: BaseHomeTrucksRVAdapterItem<*>) {
        when(actionId){
            HomeTrucksVehicleFilterAction -> {
                showVehicleFilterDialog()
            }

            HomeTrucksAvailabilityFilterAction->{
                showAvailabilityFilterDialog()
            }

            HomeTrucksSizeFilterAction -> {
                showSizeFilterDialog()
            }

            HomeTrucksWarningAction_NoTrucks ->{
                context?.let { startActivityForResult(truckIntent(context!!), REQCODE_ADD_TRUCK) }
            }

            HomeTrucksPriorityAction -> {
                context?.let { startActivityForResult(truckIntent(context!!), REQCODE_ADD_TRUCK) }
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
            HomeTrucksRequestAction_EditTruck ->{
                showOptionsDialog(item.data as HomeTrucksRequestItemData , position)
            }

            HomeTrucksRequestAction_ActivateTruck -> {
                context?.let {
                    ActivateTruckDialog(context!!, item.data as HomeTrucksRequestItemData, viewModel, userPrefs, analyticsUtil, uiUtils,position).show()
                }
            }
        }
    }


    private fun showAddTruckDialog(items: List<TruckFrequentItem>) {
        val dialog = Dialog(context!!)
        val bindingDialog= DialogBottomTruckAddBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.containerTrucks.removeAllViews()
        items.forEachIndexed { index, item ->
            val itemBinding = createTruckFrequentItem(bindingDialog)
            itemBinding.data = item
            itemBinding.root.setOnClickListener{
                context?.let { startActivityForResult(truckIntent(context!!,item.truckType, item.truckSize, item.capacity, item.minCap, item.maxCap,item.sourcedAs)
                    , REQCODE_ADD_TRUCK) }
                dialog.dismiss()
            }

            bindingDialog.containerTrucks.addView(itemBinding.root, index)
        }
        bindingDialog.closeBtn.setOnClickListener{
            dialog.dismiss()
        }

        bindingDialog.addTruckLayout.setOnClickListener{
            context?.let { startActivityForResult(truckIntent(context!!), REQCODE_ADD_TRUCK) }
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    private fun showOptionsDialog(data: HomeTrucksRequestItemData, position: Int) {
        val dialog = Dialog(context!!)
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
            context?.let {  EditTruckDialog(context!!, data, viewModel, userPrefs, analyticsUtil, uiUtils,position).show()}
            dialog.dismiss()

        }
        bindingDialog.deactivateTruckLayout.setOnClickListener {
            showDeactivateDialog(position, data)
            dialog.dismiss()
        }

        bindingDialog.deleteTruckLayout.setOnClickListener{
            uiUtils.showProgress()
            analyticsUtil.trackEvent(
                EVENT_DELETE_TRUCK,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID),
                mutableListOf(userPrefs.userId(), data.inventoryId)
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
                analyticsUtil.trackEvent(
                    EVENT_DEACTIVATE_TRUCK,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID),
                    mutableListOf(userPrefs.userId(), data.inventoryId)
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

        // Initialize an array of vehicles
        val arraySize = viewModel.truckSizeData.toTypedArray()

        val arrayChecked = booleanArrayOf(false,false,false)

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
                    filterSizeTypes  = filterSizeTypes +item
                }
            }
            viewModel.sizeFilter = filterSizeTypes.joinToString( separator = ",") {it}
            refreshData()

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showAvailabilityFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayAvailable = arrayOf("Available","Not Available", "Active")

        val arrayChecked = booleanArrayOf(false,false,false)

        val availableFilterList = mutableListOf<String>()

        if (viewModel.availabilityFilter.isNotNullOrEmpty()) {
            availableFilterList.addAll(viewModel.availabilityFilter!!.split(","))
        }

        if (availableFilterList.isNotEmpty()) {
            for (item in availableFilterList) {
                if (arrayAvailable.contains(item))
                {
                    arrayChecked[arrayAvailable.indexOf(item)] = true
                }
            }
        }

        val builder = AlertDialog.Builder(context)

        builder.setTitle("-- Select Availability --")

        builder.setMultiChoiceItems(arrayAvailable, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

            var filterAvailabilityTypes = listOf<String>()
            for (item in arrayAvailable) {
                if (arrayChecked[arrayAvailable.indexOf(item)]) {
                    filterAvailabilityTypes  = filterAvailabilityTypes + item
                }
            }
            viewModel.availabilityFilter = filterAvailabilityTypes.joinToString( separator = ",") {it}
            refreshData()

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showVehicleFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayBody = arrayOf("open","closed","trailer")

        val arrayChecked = booleanArrayOf(false,false,false)

        val currentVehicleFilterList = mutableListOf<String>()

        if (viewModel.bodyTypeFilter.isNotNullOrEmpty()) {
            currentVehicleFilterList.addAll(viewModel.bodyTypeFilter!!.split(","))
        }

        if (currentVehicleFilterList.isNotEmpty()) {
            for (vehicle in currentVehicleFilterList) {
                if (arrayBody.contains(vehicle))
                {
                    arrayChecked[arrayBody.indexOf(vehicle)] = true
                }
            }
        }

        val builder = AlertDialog.Builder(context)

        builder.setTitle("-- Select Size --")

        builder.setMultiChoiceItems(arrayBody, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

            var filterBodyTypes = listOf<String>()
            for (vehicle in arrayBody) {
                if (arrayChecked[arrayBody.indexOf(vehicle)]) {
                    filterBodyTypes  = filterBodyTypes + vehicle
                }
            }
            viewModel.bodyTypeFilter = filterBodyTypes.joinToString( separator = ",") {it}
            refreshData()

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQCODE_ADD_TRUCK -> {
                if( data != null  && data.getStringExtra("Added") == "Truck Added"){
                    refreshData()
                }
            }

        }
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

    /** Create new frequent truck item*/
    private fun createTruckFrequentItem(binding: DialogBottomTruckAddBinding)=
        ViewFrequentTruckItemBinding.inflate(layoutInflater, binding.containerTrucks, false)


}

package com.delhivery.axle.ui.biddetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.TextKeyListener.Capitalize
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.databinding.ActivityLoadBidDetailsBinding
import com.delhivery.axle.databinding.ActivityPlacementsBidDetailsBinding
import com.delhivery.axle.databinding.DialogPlacementDetailsEditBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.AddTruckBottomSheetDialog
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.home.fragments.placements.PlacementTypes
import com.delhivery.axle.ui.placementdetails.REFRESH_ON_BACK_PLACEMENT
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.BidSuccessInterface
import com.delhivery.axle.utils.DetailsSubmittedSuccessInterface
import com.delhivery.axle.utils.EVENT_ADD_TRUCK_INITIATE
import com.delhivery.axle.utils.EVENT_BID_REVISE_INITIATED
import com.delhivery.axle.utils.EVENT_ORDER_DETAILS_BID_INITIATE
import com.delhivery.axle.utils.LoadTypeUtils
import com.delhivery.axle.utils.PROPERTY_BID_COUNT
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_ORDER_LOWEST_BID_VALUE
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_SUB_SOURCE
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.VALUE_APP_FLOW
import com.delhivery.axle.utils.VALUE_BANNER
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.getSerializableExtra
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.gson.Gson
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.abs

/**
 * Bid detail screen
 */
class PlacementsBidDetailsActivity :
    BaseActivity<ActivityPlacementsBidDetailsBinding, BidDetailsViewModel>(), BulkBidsRVAdapterInterface,
    BidSuccessInterface {

    init {
        hasInlineProgress = true
    }

    private val TAG = PlacementsBidDetailsActivity::class.java.simpleName

    @Inject
    lateinit var userPrefs: UserPrefs

    @Inject
    lateinit var autoCompleteUtils: AutoCompleteUtils

    private var isValidVehicleNumber = false
    private var isValidDriverName = false
    private var isValidDriverNumber = false
    private var hasUserInteractedWithDriverName = false
    private var lastValidationTime = 0L
    private var isValidationDisabled = false
    private var lastEnableSubmitTime = 0L
    private var homePlacementsItemData: HomePlacementsItemData? = null

    companion object {
        private const val REQCODE_PICK_CONTACT = 2001
    }

    override fun getViewModelClass() = BidDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_placements_bid_details

    override fun requireConnection() = true

    var bidEndingTime: String = ""
    var source: String = VALUE_APP_FLOW
    var subSource: String = "NA"
    var reviseInitiated: Boolean = false
    var oldAmountbids = ""
    var isFirstBid = false
    var buttonVisible = false
    var bottomLayVisible = false
    private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }
    var uploadArray: ArrayList<Pair<String, String?>> = ArrayList()
    var stopArray: ArrayList<String> = ArrayList()
    var ellp = true
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    private var amount = 0
    private var pmtRate = 0
    private var forPlacement = false
    private var addressDetailAdapter : AddressDetailAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("onCreate","onCreate")
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("PlacementsBidDetailsActivity_SetupTime")
        activitySetupTrace?.start()
        /* validate intent */
        try {
            require(
                !(intent == null || (!intent.hasExtra(TransactionIdIntentKey) && !intent.hasExtra(ContractCodeIntentKey)))
            ) { "Required data $TransactionIdIntentKey or $ContractCodeIntentKey not found" }
        } catch (e: Exception) {
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })
        viewModel.restrictEventTrigger = true
        /* set transaction id */
        viewModel.placementType = intent.getStringExtra(PlacementTypeIndentKey)?:""
        viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey)?:""
        viewModel.contractCode = intent.getStringExtra(ContractCodeIntentKey)?:""
        viewModel.dmtStatus = intent.getStringExtra(RequestTypeIntentKey) ?: ""
        viewModel.fromPage = intent.getBooleanExtra(FromPage, false)
        viewModel.active = intent.getBooleanExtra(ActiveBid, false)
        source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW
        subSource = intent.getStringExtra(PROPERTY_SUB_SOURCE) ?: "NA"
        forPlacement = intent.getBooleanExtra(ForPlacementKey, false)
        if (intent?.extras?.getSerializableExtra(
                PlacementData,
                HomePlacementsItemData::class.java
            ) != null
        )
            homePlacementsItemData = intent.extras?.getSerializableExtra(
                PlacementData,
                HomePlacementsItemData::class.java
            )
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        //setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //
        title = ""
        //
        if (homePlacementsItemData?.status == PlacementTypes.Delayed.name) {
            binding.toolbarEndText.visibility = View.VISIBLE
            binding.toolbarEndText.text = "Delayed"
            binding.toolbarEndText.background = ContextCompat.getDrawable(this, R.drawable.bg_all_rounded_delayed)
        } else {
            binding.toolbarEndText.visibility = View.GONE
        }
        /* setup live data observers */
        setupLiveDataObservers()
        //
        binding.containerError.btnAction.setOnClickListener {
            refreshData()
        }
        //
        binding.refreshLayout.setOnRefreshListener {
            viewModel.refreshCalled = true
            binding.mainCl.visibility = View.GONE
            refreshData()
        }
        //fetch intial data
        refreshData()
    }

    private fun setupLiveDataObservers() {
        viewModel.progressLiveData.observe(this, ProgressObserver())
        //
        viewModel.transactionLiveData.observe(this, TransactionObserver())
        //
    }

    private fun refreshData() {
        binding.error = false
        //
        viewModel.fetchPlacementDetails()
        //
        binding.executePendingBindings()
        //
        uploadArray.clear()
        //clear intracity address livedata as well
        viewModel.addressLiveData.value = null
        binding.cardInput.routeAddress.text = ""
        //
        viewModel.pickupAddressLiveData.value = null
        //
        viewModel.destinationAddressLiveData.value = null
    }

    /**
     * Progress observer
     */
    inner class ProgressObserver : Observer<Boolean> {
        override fun onChanged(t: Boolean) {
            t.let {
                when (t) {
                    true -> {
                        binding.refreshLayout.isRefreshing = true
                        binding.refreshing = true
                    }

                    false -> {
                        binding.refreshLayout.isRefreshing = false
                        /* if (buttonVisible) {
                           binding.buttonConfirm.visibility = View.VISIBLE
                         }
                         if (bottomLayVisible) {
                           binding.bottomLay.visibility = View.VISIBLE
                         }*/
                    }
                }
            }
            binding.executePendingBindings()
        }
    }

    fun triggerPickupAndDestinationAddressCall(pickupCenterCode:String, destinationCenterCode:String){
        //
        viewModel.pickupAddressLiveData.removeObservers(this@PlacementsBidDetailsActivity)
        //
        viewModel.destinationAddressLiveData.removeObservers(this@PlacementsBidDetailsActivity)
        //
        viewModel.pickupAddressLiveData.observe(this, Observer {
            Log.d(TAG, "pickupAddressLiveData")
            it?.propertyAddressDetails?.address?.let { address ->
                viewModel.pickupAddress = address
            }
        })
        //
        viewModel.destinationAddressLiveData.observe(this, Observer {
            Log.d(TAG, "destinationAddressLiveData")
            //
            it?.propertyAddressDetails?.address?.let { address ->
                viewModel.destinationAddress = address
                //Add pickup centre name and address
                uploadArray.clear()
                uploadArray.add(Pair(binding.transaction?.routeInfo?.origin?.centerName?:"", viewModel.pickupAddress))
                //add halt centers if any, then add destination
                addHaltCenters()
                //destination centre name and address
                uploadArray.add(Pair(binding.transaction?.routeInfo?.destination?.centerName?:"", viewModel.destinationAddress))
                //notify adapter
                notifyRouteDetailsAdapter()
            }
        })

        //trigger pickup api call and destination api call will be triggered in sequence post pickup successful response
        viewModel.getPlacementFacilityAddress(
            pickupCenterCode= pickupCenterCode,
            destinationCenterCode = destinationCenterCode,
            facilityType = "pickup")
        //
        //viewModel.getPlacementFacilityAddress(destinationCenterCode, "destination")
    }

    private fun addHaltCenters() {
        binding.transaction?.routeInfo?.haltCenters?.forEach{ item ->
            uploadArray.add(Pair(item.centerName?:"", item.address?:""))
        }
    }

    private fun notifyRouteDetailsAdapter(){
        if (uploadArray.isNotEmpty()) {
            Log.d(TAG, "uploadArray.isNotEmpty")
            addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.cvRouteSection.addresslist.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                adapter = addressDetailAdapter
            }
            //hide progressbar
            binding.cvRouteSection.pbRouteDetails.visibility = View.GONE
        } else {
            //hide route details section
            binding.cvRouteSection.cvRouteContainer.visibility = View.GONE
        }
    }

    /**
     * Transaction details and UI updation Observer
     */
    inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
        override fun onChanged(t: HomeBidsRequestItemData?) {
            if (t != null) {
                t.let { _transaction ->
                    //assign placementtype to loadType var to distinguish b/w different types of loads in "HomeBidsRequestItemData" class and its functions
                    _transaction.loadType = viewModel.placementType
                    //
                    binding.placementDetailsContent.placementListingData = homePlacementsItemData
                    binding.placementDetailsContent.request = _transaction
                    binding.error = false
                    //
                    binding.transaction = _transaction
                    binding.cardInput.request = _transaction
                    //setup placement details specific views
                    binding.mainCl.visibility = View.VISIBLE
                    binding.cardInput.editBidCl.visibility = View.GONE
                    binding.cardInput.root.visibility = View.VISIBLE
                    binding.cardInput.placementCl.root.visibility = View.VISIBLE
                    placementInput()
                }


                Log.d("$TAG::placementType==>>", ""+viewModel.placementType)

                /**
                 * New address adapter code
                 */
                if(viewModel.transaction.isIntracity()){
                //if(false){
                    //hide route details section
                    binding.cvRouteSection.cvRouteContainer.visibility = View.GONE
                }else{
                    //
                    binding.cvRouteSection.cvRouteContainer.visibility = View.VISIBLE
                    //init observer trigger pickup and destination address call
                    triggerPickupAndDestinationAddressCall(
                        pickupCenterCode = binding.transaction?.routeInfo?.origin?.centerCode?:"",
                        destinationCenterCode = binding.transaction?.routeInfo?.destination?.centerCode?:"")
                }
            } else {
                binding.error = true
                binding.containerError.title = "Session Time Out"
                binding.containerError.subTitle =
                    "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
                binding.containerError.actionLabel = "REFRESH"
            }
            binding.executePendingBindings()
        }
    }

    fun enableKeyboard() {
        binding.cardInput.etBidAmount.requestFocus()

        // Delay to ensure the window is ready before showing the keyboard
        binding.cardInput.etBidAmount.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.cardInput.etBidAmount, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        super.onBackPressed()
    }


    override fun handleAction(
        actionId: String,
        position: Int,
        item: BaseBulkBidSummaryRVAdapterItem<*>
    ) {
        when (actionId) {
            EXPAND_CARD -> {
                val bidData = item.data as BulkBidSummaryItemData
                bidData.expanded = !bidData.expanded
                BidDetailsViewModel.truckNumTextViewAdded =
                    !BidDetailsViewModel.truckNumTextViewAdded
                adapter.notifyItemChanged(position)
            }

            OPEN_CONFIRMED_BID -> {

            }
        }
    }

    private fun triggerFacilityAddress(originCenterCode:String?){
        //
        binding.cardInput.pbIntracityAddress.visibility = View.VISIBLE
        //
        viewModel.getFacilityAddress(originCenterCode)
    }


    fun placementInput() {
        binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
        binding.cardInput.placementCl.driverNameError.visibility = View.GONE
        //
        //Log.d("$TAG::isIntracity", ""+viewModel.transaction.isIntracity())
        //Log.d("$TAG::viewModel.transaction", ""+Gson().toJson(viewModel.transaction))
        if (viewModel.transaction.isIntracity()) {
            //
            viewModel.addressLiveData.removeObservers(this@PlacementsBidDetailsActivity)
            viewModel.addressLiveData.observe(this@PlacementsBidDetailsActivity, Observer {
                it?.propertyAddressDetails?.address?.let { address ->
                    binding.cardInput.pbIntracityAddress.visibility = View.GONE
                    binding.cardInput.routeAddress.text = capitalize(address)
                }
            })
            //
            triggerFacilityAddress(homePlacementsItemData?.originCenterCode)
            //
            binding.cardInput.routeAddress.visibility = View.VISIBLE
            //
            binding.cardInput.mapText.visibility = View.VISIBLE
            //
            binding.cardInput.mapText.setOnClickListener {
                navigateToMap()
            }
        } else {
            binding.cardInput.pbIntracityAddress.visibility = View.GONE
            binding.cardInput.routeAddress.visibility = View.GONE
            binding.cardInput.mapText.visibility = View.GONE

        }

        //old working code below done by Rahul - no change

        viewModel.updateVehicleDetails.observe(this, Observer {
            if (it) {
                uiUtils.hideProgress()
                REFRESH_ON_BACK_PLACEMENT = true
                when (homePlacementsItemData?.loadType) {
                    LoadTypes.intracityAdhoc.name -> showIntracityAdhocSuccessDialog()
                    else -> showSuccessEditDialog("Submitted successfully!")
                }
            } else {
                uiUtils.hideProgress()
            }
        })

        autoCompleteUtils.autoCompleteTruck(binding.cardInput.placementCl.editAutoCompleteTrucks) {
            // Ensure UI operations run on main thread
            binding.cardInput.placementCl.editAutoCompleteTrucks.post {
                if (it == "Add New Truck") {
                    binding.cardInput.placementCl.editAutoCompleteTrucks.text.clear()
                    this.let { showAddTruckBottomSheet() }
                } else if (validateTruckNumber(it)) {
                    isValidVehicleNumber = true
                    binding.cardInput.placementCl.vehicleError.visibility = View.GONE
                    binding.cardInput.placementCl.editTextVehicleNumber.text = it
                    binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
                    binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
                    // Don't call enableSubmitPlacement here - let TextWatcher handle it
                } else {
                    binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
                    binding.cardInput.placementCl.vehicleError.text =
                        "Please enter valid vehicle Number"
                    isValidVehicleNumber = false
                    // Don't call enableSubmitPlacement here - let TextWatcher handle it
                }
            }
        }

        binding.cardInput.placementCl.editTextVehicleNumber.setOnClickListener {
            binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.GONE
            binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.VISIBLE
            binding.cardInput.placementCl.editAutoCompleteTrucks.focusClick()
            isValidVehicleNumber = false
            enableSubmitPlacement()
        }

        binding.cardInput.placementCl.editTextVehicleNumber.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Driver name field click handling - now just focus the single field
        binding.cardInput.placementCl.editAutoCompleteDriverName.setOnClickListener {
            hasUserInteractedWithDriverName = true
            //binding.cardInput.placementCl.editAutoCompleteDriverName.focusClick()
            binding.cardInput.placementCl.editAutoCompleteDriverName.requestFocus()
            enableSubmitPlacement()
        }

        // Simple TextWatcher for manual typing validation
        binding.cardInput.placementCl.editAutoCompleteDriverName.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (text.length >= 2) {
                    isValidDriverName = true
                    binding.cardInput.placementCl.driverNameError.visibility = View.GONE
                } else {
                    isValidDriverName = false
                    if (hasUserInteractedWithDriverName && text.isNotEmpty()) {
                        binding.cardInput.placementCl.driverNameError.visibility = View.VISIBLE
                        binding.cardInput.placementCl.driverNameError.text =
                            "Please enter valid driver name"
                    } else {
                        binding.cardInput.placementCl.driverNameError.visibility = View.GONE
                    }
                }
                enableSubmitPlacement()
            }
        })

        // Text change handling is now managed by the AutoCompleteUtils

        homePlacementsItemData?.vehicleNumber?.let {
            Log.i("vehicleNumber", it)
            isValidVehicleNumber = true
            binding.cardInput.placementCl.editTextVehicleNumber.text = it
            binding.cardInput.placementCl.editAutoCompleteTrucks.setText(it)
            binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
            binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
            enableSubmitPlacement()
        }

        homePlacementsItemData?.driverName?.let {
            Log.i("DriverNameValidation", "Initial setup: Found existing driver name: '${it}'")
            isValidDriverName = true
            binding.cardInput.placementCl.editAutoCompleteDriverName.setText(it)
            binding.cardInput.placementCl.driverNameError.visibility = View.GONE
            enableSubmitPlacement()
        }

        homePlacementsItemData?.driverPhone?.let {
            isValidDriverNumber = true
            binding.cardInput.placementCl.editDriverNumber.setText(it)
            enableSubmitPlacement()
        }

        if (homePlacementsItemData?.status == "Marked-in") {
            binding.cardInput.placementCl.editTextVehicleNumber.isEnabled = false
            binding.cardInput.placementCl.editAutoCompleteDriverName.isEnabled = false
            binding.cardInput.placementCl.editDriverNumber.isEnabled = false
            disableSubmitPlcButton()
        }

        binding.cardInput.placementCl.btnSubmit.setOnClickListener {
            submitPlacementDetails()
        }

        // SIMPLIFIED APPROACH - Use basic autocomplete without complex validation
        autoCompleteUtils.autoCompleteDriverNameWithPhone(
            binding.cardInput.placementCl.editAutoCompleteDriverName,
            { binding.cardInput.placementCl.editTextVehicleNumber.text.toString() }
        ) { driverData ->

            driverData.driverName?.let {
                isValidDriverName = it.length >= 2
            }
            // Populate phone number when driver is selected from dropdown
            driverData.driverPhone?.let {
                binding.cardInput.placementCl.editDriverNumber.setText(it)
                isValidDriverNumber = validatePhoneNumber(it)
            }
            enableSubmitPlacement()
        }

        binding.cardInput.placementCl.editDriverNumber.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    val input = s.trim().toString()
                    isValidDriverNumber = input.length == 10 && validatePhoneNumber(input)
                    // Don't call enableSubmitPlacement here - let TextWatcher handle it
                } else {
                    isValidDriverNumber = false
                    // Don't call enableSubmitPlacement here - let TextWatcher handle it
                }
            }
        })

        binding.cardInput.placementCl.btnContactPicker.setOnClickListener {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(pickContactIntent, REQCODE_PICK_CONTACT)
        }
    }

    private fun navigateToMap() {
        try {
            val gmmIntentUri =
                Uri.parse("geo:0,0?q=${homePlacementsItemData?.originCenterLat},${homePlacementsItemData?.originCenterLong}" + "(" + homePlacementsItemData?.originCenterName + ")")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddTruckBottomSheet() {
        val dialog = AddTruckBottomSheetDialog(
            this,
            viewModelFactory,
            userPrefs,
            autoCompleteUtils,
            onTruckAdded = { truckNumber ->
                showSuccessEditDialog("Truck added successfully!")
                binding.cardInput.placementCl.editTextVehicleNumber.text = truckNumber
                binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
                binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
                isValidVehicleNumber = true
                // Don't call enableSubmitPlacement here - let TextWatcher handle it
            }
        )
        dialog.show()
    }

    private fun enableSubmitPlacement() {
        Log.d(TAG, "enableSubmitPlacement")
        // DEBOUNCE PROTECTION - Prevent rapid calls to enableSubmitPlacement (reduced from 500ms to 100ms)
        /* val currentTime = System.currentTimeMillis()
         val timeSinceLastCall = currentTime - lastEnableSubmitTime
         if (timeSinceLastCall < 100) { // Less than 100ms since last call
           Log.i("validate", "Skipping enableSubmitPlacement - too frequent (${timeSinceLastCall}ms since last)")
           return
         }
         lastEnableSubmitTime = currentTime*/

        // Re-validate all conditions
        isValidVehicleNumber =
            validateTruckNumber(binding.cardInput.placementCl.editTextVehicleNumber.text.toString())

        // Re-validate driver name
        val driverNameText =
            binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString()
        isValidDriverName = driverNameText.length >= 2

        // Re-validate driver phone
        val driverPhoneText = binding.cardInput.placementCl.editDriverNumber.text.toString()
        isValidDriverNumber = driverPhoneText.length == 10 && validatePhoneNumber(driverPhoneText)

        val errorVisibility =
            if (binding.cardInput.placementCl.driverNameError.visibility == View.VISIBLE) "VISIBLE" else "GONE"
        Log.i(
            "validate",
            "DriverName: '$driverNameText', length: ${driverNameText.length}, isValidDriverName: $isValidDriverName, isValidVehicleNumber: $isValidVehicleNumber, isValidDriverNumber: $isValidDriverNumber, hasUserInteracted: $hasUserInteractedWithDriverName, errorVisibility: $errorVisibility"
        )

        val isMarkedIn = homePlacementsItemData?.status == "Marked-in"
        val allValid =
            isValidDriverName && isValidVehicleNumber && isValidDriverNumber && !isMarkedIn

        Log.i(
            "validate",
            "All conditions: driverName=$isValidDriverName, vehicle=$isValidVehicleNumber, phone=$isValidDriverNumber, markedIn=$isMarkedIn, allValid=$allValid"
        )

        if (allValid) {
            enableSubmitPlcButton()
        } else {
            disableSubmitPlcButton()
        }
    }

    private fun disableSubmitPlcButton() {
        binding.cardInput.placementCl.btnSubmit.isEnabled = false
    }

    private fun enableSubmitPlcButton() {
        binding.cardInput.placementCl.btnSubmit.isEnabled = true
    }

    private fun validateTruckNumber(number: String): Boolean {
        val pattern = Pattern.compile(
            "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
        )
        return pattern.matcher(number).matches()
    }

    private fun validatePhoneNumber(number: String): Boolean {
        val sameDigitsReg = "^([0-9])\\1*$"
        var result = false
        val sameDigitsPattern = Pattern.compile(sameDigitsReg)
        if (!sameDigitsPattern.matcher(number).matches()) {
            val standardNumberPattern = Pattern.compile("^[6-9]{1}[0-9]{9}$")
            result = standardNumberPattern.matcher(number).matches()
        }
        return result
    }

    private fun submitPlacementDetails() {
        if (homePlacementsItemData == null) {
            uiUtils.showToast("Placement data not available")
            return
        }
        if (binding.cardInput.placementCl.editAutoCompleteTrucks.visibility == View.VISIBLE) {
            binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
            binding.cardInput.placementCl.vehicleError.text = "Please select a valid vehicle number"
            isValidVehicleNumber = false
            binding.cardInput.placementCl.editAutoCompleteTrucks.errorAnimate()
            enableSubmitPlacement()
        } else {
            binding.cardInput.placementCl.vehicleError.visibility = View.GONE

            try {
                val isOrionType =
                    homePlacementsItemData?.loadType?.lowercase(Locale.getDefault())
                        ?.contains("orion") ?: false

                val vehicleType = if (isOrionType) {
                    null
                } else if (homePlacementsItemData?.loadType?.lowercase(Locale.getDefault())
                        ?.contains("adhoc") == true
                ) {
                    "adhoc"
                } else {
                    "regular"
                }
                val contractType = if (isOrionType) {
                    null
                } else if (homePlacementsItemData?.loadType!!.lowercase(Locale.getDefault())
                        .contains("ftl")
                ) {
                    "ftl"
                } else {
                    "intracity"
                }
                val action = if (isOrionType) {
                    "placement_on_orion"
                } else if (homePlacementsItemData?.vehicleNumber == null) {
                    "add_placement"
                } else {
                    "update_placement"
                }
                try {
                    uiUtils.showProgress()
                    val updateVehicleDetailsRequest = UpdateVehicleDetailsRequest(
                        binding.cardInput.placementCl.editTextVehicleNumber.text.toString(),
                        binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString(),
                        binding.cardInput.placementCl.editDriverNumber.text.toString(),
                        contractType,
                        vehicleType,
                        homePlacementsItemData?.vehicleType ?: "",
                        homePlacementsItemData?.transporterSupplierId ?: "",
                        contractId = if (LoadTypeUtils.isContractCodeRequired(homePlacementsItemData?.loadType?:"")) homePlacementsItemData?.contractId else null,
                        homePlacementsItemData?.transporterId ?: 0,
                        action,
                        if (isOrionType) null else homePlacementsItemData?.reportingTime ?: "",
                        if (isOrionType) null else homePlacementsItemData?.originCenterCode ?: "",
                        if (isOrionType) null else homePlacementsItemData?.vehicleNumber,
                        if (isOrionType) null else homePlacementsItemData?.vehicleId,
                        if (isOrionType) null else homePlacementsItemData?.driverName,
                        if (isOrionType) null else homePlacementsItemData?.driverPhone,
                        transactionId = if (LoadTypeUtils.isTransactionIdRequired(homePlacementsItemData?.loadType?:"")) homePlacementsItemData?.transactionId else null
                    )
                    viewModel.updateVehicleDetails(updateVehicleDetailsRequest)
                } catch (e: Exception) {
                    uiUtils.showToast("Something went wrong")
                    uiUtils.hideProgress()
                }
            } catch (e: IllegalArgumentException) {
                uiUtils.hideProgress()
                Log.e("PlacementDetails", e.toString())
            }
        }
    }

    private fun showIntracityAdhocSuccessDialog() {
        val title = getString(string.title_dialog_success)
        val subTittle = getString(string.sub_title_dialog_success)
        val playStoreLink = getString(string.driver_app_link)
        val hindiVideoLink = getString(string.hindi_video_link)
        val englishVideoLink = getString(string.english_video_link)

        val placementData = homePlacementsItemData
        Log.d("placementData=====>>>>", Gson().toJson(placementData))
        val ticketId = placementData?.transactionId ?: ""
        val reportingCentre = generateReportingCentreLink(placementData!!)
        val reportingTime = formatReportingTime(placementData.reportingTime)

        dialogUtils.showDetailsSubmittedSuccessDialog(
            title = title,
            subTittle = subTittle,
            playStoreLink = playStoreLink,
            ticketId = ticketId,
            reportingCentre = reportingCentre,
            reportingTime = reportingTime,
            hindiVideoLink = hindiVideoLink,
            englishVideoLink = englishVideoLink,
            dialogInterface = object : DetailsSubmittedSuccessInterface {
                override fun onDialogDismissed() {
                    REFRESH_ON_BACK_PLACEMENT = true
                    /*  Log.d("onDialogDismissed===>>>","Dialog dismissed")
                      Handler(Looper.myLooper()!!).postDelayed({
                        REFRESH_ON_BACK_PLACEMENT = true
                        finish()
                      }, 100)*/
                }
            }
        )
    }

    private fun generateReportingCentreLink(placementData: HomePlacementsItemData): String {
        return if (placementData.originCenterLat != null && placementData.originCenterLong != null) {
            "https://maps.google.com/?q=${placementData.originCenterLat},${placementData.originCenterLong}"
        } else {
            val location = placementData.originCenterName ?: ""
            if (location.isNotEmpty()) {
                "https://maps.google.com/?q=$location"
            } else {
                ""
            }
        }
    }

    private fun formatReportingTime(reportingTime: String?): String {
        return if (!reportingTime.isNullOrEmpty()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                val outputFormat = SimpleDateFormat("dd MMM, hh:mm a")
                val date = inputFormat.parse(reportingTime)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "--:--"
            }
        } else {
            "--:--"
        }
    }

    private fun showSuccessEditDialog(msg: String) {
        val dialog = Dialog(this)
        val bindingDialog = DialogPlacementDetailsEditBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.titleText.text = msg
        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }
        if (!this.isFinishing)
            dialog.show()
//    if(!this.isFinishing)
//      Handler(Looper.myLooper()!!).postDelayed({
//        dialog.dismiss()
//        REFRESH_ON_BACK_PLACEMENT = true
//        finish()
//      }, 2000)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_PICK_CONTACT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val contactUri: Uri? = data.data
                    contactUri?.let {
                        val cursor: Cursor? = contentResolver.query(it, null, null, null, null)
                        cursor?.use { c ->
                            if (c.moveToFirst()) {
                                val numberIndex =
                                    c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex != -1) {
                                    val phoneNumber = c.getString(numberIndex)
                                    var trimmedNumber = phoneNumber?.replace("+91", "")?.replaceFirst("^0+".toRegex(), "")?.trim()
                                    trimmedNumber = trimmedNumber?.replace(" ", "")?.trim()
                                    Log.d("trimmedNumber==>>>",trimmedNumber?:"")
                                    binding.cardInput.placementCl.editDriverNumber.setText(
                                        trimmedNumber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun bidPlacedSuccess(success: Boolean) {
        if (success)
            startActivity(homeActivityIntent("load", this@PlacementsBidDetailsActivity))

    }

}

/* intent keys */
private const val PlacementTypeIndentKey = "placementType"
private const val TransactionIdIntentKey = "transaction_id"
private const val ContractCodeIntentKey = "contractCode"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
private const val FromPage = "from_page"
private const val ActiveBid = "active_bid"
private const val ForPlacementKey = "for_placement"
private const val PlacementData = "placement_data"

/**
 * Bid details intent
 */
fun placementsBidDetailsIntent(
    placementType:String,
    transactionId: String?,
    contractCode: String?,
    context: Context,
    requestType: String? = null,
    fromBidsPage: Boolean = false,
    active: Boolean = false,
    source: String? = VALUE_APP_FLOW,
    subSource: String? = "NA",
    forPlacement: Boolean = false,
    homePlacementsItemData: HomePlacementsItemData? = null
) = Intent(context, PlacementsBidDetailsActivity::class.java).apply {
    if (placementType.isNotNullOrEmpty()) putExtra(PlacementTypeIndentKey, placementType)
    if (transactionId.isNotNullOrEmpty()) putExtra(TransactionIdIntentKey, transactionId)
    if (contractCode.isNotNullOrEmpty()) putExtra(ContractCodeIntentKey, contractCode)
    if (requestType != null) putExtra(RequestTypeIntentKey, requestType)
    putExtra(FromPage, fromBidsPage)
    putExtra(ActiveBid, active)
    putExtra(PROPERTY_SOURCE, source)
    putExtra(PROPERTY_SUB_SOURCE, subSource)
    putExtra(ForPlacementKey, forPlacement)
    putExtra(PlacementData, homePlacementsItemData)
}


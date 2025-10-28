package com.delhivery.axle.ui.contractDetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TransactionStatus
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.data.home.bids.HaltCenters
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.PaymentSlabs
import com.delhivery.axle.data.home.bids.SecondaryReportingCenters
import com.delhivery.axle.data.home.placements.HOME_PLACEMENT_ITEM_DATA
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.databinding.ActivityContractDetailsBinding
import com.delhivery.axle.databinding.DialogContractsBidSuccessBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.BidDetailsContractCancelled
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_ContractResult
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_EditBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_LoadingBids
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBidFirst
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.dialogs.AddTruckBottomSheetDialogFragment
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.home.fragments.contracts.REFRESH_ON_BACK
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.BidSuccessInterface
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.EVENT_ADD_TRUCK_INITIATE
import com.delhivery.axle.utils.EVENT_HOME_CONTRACT_CARD_CLICK
import com.delhivery.axle.utils.EVENT_REVISE_CONTRACT_BID
import com.delhivery.axle.utils.EVENT_SUBMIT_CONTRACT_BID
import com.delhivery.axle.utils.PROPERTY_BID_AMOUNT_DIFF
import com.delhivery.axle.utils.PROPERTY_CONTRACT_TYPE
import com.delhivery.axle.utils.PROPERTY_IS_FLEXIBLE
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_ADD_TRUCK
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_PLACEMENT
import com.delhivery.axle.utils.VALUE_APP_FLOW
import com.delhivery.axle.utils.VALUE_BANNER
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.getSerializableExtra
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.abs
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.text.method.TextKeyListener.Capitalize
import android.view.Gravity
import android.view.WindowManager
import com.delhivery.axle.databinding.ActivityPlacementsContractDetailsBinding
import com.delhivery.axle.databinding.DialogPlacementDetailsEditBinding
import com.delhivery.axle.ui.biddetails.PlacementsBidDetailsActivity
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.home.fragments.placements.PlacementTypes
import com.delhivery.axle.ui.placementdetails.REFRESH_ON_BACK_PLACEMENT
import com.delhivery.axle.utils.DetailsSubmittedSuccessInterface
import com.delhivery.axle.utils.LoadTypeUtils
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.Gson
import androidx.core.view.isVisible
import com.delhivery.axle.utils.WindowInsetsUtils

class PlacementsContractDetailsActivity: BaseActivity<ActivityPlacementsContractDetailsBinding, ContractDetailsViewModel>(),BidSuccessInterface {

    private val TAG = PlacementsContractDetailsActivity::class.java.simpleName

    init {
        hasInlineProgress = true
    }
    companion object {
        private const val REQCODE_PICK_CONTACT = 2001
    }
    @Inject lateinit var userPrefs: UserPrefs
    @Inject
    lateinit var autoCompleteUtils: AutoCompleteUtils
    var routesArray:ArrayList<HaltCenters> = ArrayList()
    var flexibleReportingCentersArray:ArrayList<SecondaryReportingCenters> = ArrayList()
    var paymentSlabsArray:ArrayList<PaymentSlabs> = ArrayList()
    var source= VALUE_APP_FLOW
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    private var amount = 0
    private var pmtRate = 0
    private var isChecked = false
    private var isValidBidAmount = false
    private var isValidTripCommit = false
    private var isValidVehicleNumber = false
    private var isValidPlacementDays = false
    private var forPlacement = false
    private var isValidDriverNumber = false
    private var isValidDriverName = false
    private var hasUserInteractedWithDriverName = false
    private var isDriverSelectionInProgress = false
    private var lastValidationTime = 0L
    private var isValidationDisabled = false
    private var lastEnableSubmitTime = 0L
    private var homePlacementsItemData:HomePlacementsItemData?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }

        /* validate intent */
        validateIntentData()

        //fetch intent data
        fetchIntentData()

        //init firebase performance
        initFirebasePerformance()

        //set Actionbar
        setupActionBar()

        //setup live data observers
        setupLiveDataObservers()

        //
        binding.refreshLayout.setOnRefreshListener {
            binding.mainCl.visibility = View.GONE
            fetchData()
        }

        //
        binding.containerError.btnAction.setOnClickListener {
            binding.mainCl.visibility = View.GONE
            fetchData()
        }

        //fetch placements details data
        fetchData()
    }

    private fun setupLiveDataObservers() {
        /* setup live data observers */
        viewModel.progressLiveData.observe(this, ProgressObserver())
        //
        viewModel.transactionLiveData.observe(this, TransactionObserver())
        //
        viewModel.hideProgress.observe(this, Observer {
            if(it){
                uiUtils.hideProgress()
                //       binding.bottomLay.visibility = View.VISIBLE
            }
        })
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //
        title = ""
        //
        if(homePlacementsItemData?.status==PlacementTypes.Delayed.name){
            binding.toolbarEndText.visibility = View.VISIBLE
            binding.toolbarEndText.text = "Delayed"
            binding.toolbarEndText.background = ContextCompat.getDrawable(this, R.drawable.bg_all_rounded_delayed)
        }else{
            binding.toolbarEndText.visibility = View.GONE
        }
    }

    private fun initFirebasePerformance() {
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("PlacementsContractDetailsActivity_SetupTime")
        activitySetupTrace?.start()
    }

    private fun validateIntentData() {
        try {
            require(
                !(intent == null || (!intent.hasExtra(TransactionIdIntentKey) && !intent.hasExtra(ContractCodeIntentKey)))
            ) { "Required data ${TransactionIdIntentKey} or ${ContractCodeIntentKey} not found" }
        } catch (e: Exception) {
            finish()
        }
    }

    private fun fetchIntentData() {
        /* fetch intent data */
        viewModel.placementType = intent.getStringExtra(PlacementTypeIndentKey)?:""
        viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey)?:""
        //viewModel.transactionId = ""
        viewModel.contractCode = intent.getStringExtra(ContractCodeIntentKey)?:""
        //viewModel.contractCode = ""
        //
        viewModel.requestType = RequestType.Contract.type
        //
        source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW
        //
        forPlacement = intent.getBooleanExtra(ForPlacementKey,false)
        //
        if( intent?.extras?.getSerializableExtra(PlacementData, HomePlacementsItemData::class.java)!=null)
            homePlacementsItemData =  intent.extras?.getSerializableExtra(PlacementData,HomePlacementsItemData::class.java)
    }

    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
            "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
        )
        return pattern.matcher(number).matches()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

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
                    }
                }
            }
            binding.executePendingBindings()
        }
    }

    /**
     * Transaction details and UI updation Observer
     */
    inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
        override fun onChanged(t: HomeBidsRequestItemData) {
            if (t != null) {
                t.let { _transaction ->
                    Log.d(TAG, "TransactionObserver-START")
                    //assign placementtype to loadType var to distinguish b/w different types of loads in "HomeBidsRequestItemData" class and its functions
                    _transaction.loadType = viewModel.placementType
                    //
                    binding.error = false
                    binding.transaction = _transaction
                    //set request/ transaction for route details view
                    binding.routeDetails.transaction = _transaction
                    binding.routeDetails.placementListingData = homePlacementsItemData
                    //set request for driver details view
                    binding.cardInput.request = _transaction
                    //set request for vehicle details view
                    binding.vehicleDetails.transaction = _transaction
                    binding.vehicleDetails.placementListingData = homePlacementsItemData
                    //set request for payment details view
                    binding.paymentInfo.transaction = _transaction
                    binding.paymentInfo.placementListingData = homePlacementsItemData
                    //set request for guidelines view
                    binding.layoutGuidelines.transaction = _transaction
                    binding.layoutGuidelines.placementListingData = homePlacementsItemData
                    binding.layoutGuidelines.layoutGuidelines.visibility = View.VISIBLE
                    //set default items to visible
                    binding.mainCl.visibility = View.VISIBLE
                    binding.vehicleDetails.vehicleDetails.visibility = View.VISIBLE
                    binding.cardInput.editBidCl.visibility = View.GONE
                    binding.cardInput.root.visibility = View.VISIBLE
                    binding.cardInput.placementCl.root.visibility = View.VISIBLE

                    //TODO
                    // Check the correct and updated fields to be sent to capture event
                    /*analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_CONTRACT_CARD_CLICK,
                        mutableListOf(
                            PROPERTY_USER_ID,
                            PROPERTY_PHONE_NO, PROPERTY_ORDER_ID, PROPERTY_STATUS, PROPERTY_CONTRACT_TYPE,
                            PROPERTY_SOURCE, PROPERTY_IS_FLEXIBLE
                        ),
                        mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",
                            _transaction.uuid ?: " ",_transaction.contractEventStatusText(),
                            _transaction.contractType?:"",source,_transaction.isFlexible.toString()
                        )
                    )*/

                    //setup new placement details code below
                    placementInput()

                    //Insert code below for managing other sections of the page
                    if(viewModel.transaction.isIntracity()){
                        //insert intracity specific reporting city section with city and reporting time
                        binding.routeDetails.root.visibility = View.VISIBLE
                        //binding.routeDetails.intraCityRouteDetails.visibility = View.VISIBLE

                        //set reporting city name and other details
                        //binding.routeDetails.intraCityTvHubCity.text = _transaction.routeInfo?.origin?.centerName?:""
                        binding.routeDetails.intraCityTvHubCity.text = StringUtils.capitalize(_transaction.routeInfo?.origin?.centerName)

                        //binding.routeDetails.intraCityTvCity.text = homePlacementsItemData?.origin?:""
                        Log.d("homePlacementsItemData", ""+Gson().toJson(homePlacementsItemData))

                        //check if reporting city is present or else hide the field
                        if(viewModel.propertyAddressData?.propertyCity.isNotNullOrEmpty()){
                            binding.routeDetails.intraCityTvCity.visibility = View.VISIBLE
                            binding.routeDetails.intraCityTvCity.text = StringUtils.capitalize(viewModel.propertyAddressData?.propertyCity)
                        }else{
                            binding.routeDetails.intraCityTvCity.visibility = View.GONE
                        }

                        //binding.routeDetails.intraCityTvState.text = _transaction.routeInfo?.origin?.centerState?:""
                        binding.routeDetails.intraCityTvState.text = StringUtils.capitalize(_transaction.routeInfo?.origin?.centerState)

                        //handle map navigation UI view
                        //set visible map text view
                        binding.routeDetails.intraCityTvMapView.visibility = View.VISIBLE
                        //set click listener on map
                        binding.routeDetails.intraCityTvMapView.setOnClickListener {
                            navigateToMap()
                        }

                        //handle flexible and fixed reporting tag
                        //check if intracity regular - show fixed reporting tag
                        if(viewModel.transaction.isIntracityRegular()) {
                            //show fixed reporting tag
                            binding.routeDetails.fixedIntracityTv.visibility = View.VISIBLE
                            binding.routeDetails.fixedIntracityTv.text = StringUtils.capitalize(getString(string.action_fixed_intracity))
                            //set fixed reporting icon
                            binding.routeDetails.fixedIntracityTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place,0,0,0)
                        }else if(homePlacementsItemData?.ticketFlexibleContractId != null){
                            //if "ticket_flexible_contract_id" != null - show flexible reporting tag
                            //show flexible reporting tag
                            binding.routeDetails.fixedIntracityTv.text = StringUtils.capitalize(getString(string.action_flexible_intracity))
                            //set flexible reporting icon
                            //binding.allIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
                            //binding.flexibleIntracityToggle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                            binding.routeDetails.fixedIntracityTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_multiple_location_blue,0,0,0)
                        }else {
                            //if "ticket_flexible_contract_id" == null - Don't show flexible reporting tag
                            //hide tag
                            binding.routeDetails.fixedIntracityTv.visibility = View.GONE
                        }

                        //set reporting time visible
                        binding.routeDetails.intraCityReportingTime.visibility = View.VISIBLE
                        binding.routeDetails.intraCityReportingTime.text = homePlacementsItemData?.placementsOnlyFormatReportingTime()
                    }else{

                        //manged from data binding
                        //hide intracity route details section
                        //binding.routeDetails.intraCityRouteDetails.visibility = View.GONE

                        //prepare routes data if intercity and add into routes array
                        prepareRoutesData()

                        //setup adpater
                        setupRouteAdapter()
                    }

                    //TODO
                    //REMOVE THIS CODE
                    //prepare routes data if intercity and add into routes array
                    //prepareRoutesData()

                    //setup adpater
                    //setupRouteAdapter()

                    //insert route schedule adapter with arrival and departure time - for intercity case

                    //insert vehicle details section
                    binding.vehicleDetails.vehicleDetailsLabel.visibility = View.VISIBLE
                    //nep
                    //binding.vehicleDetails.permitRequirementText.visibility = View.VISIBLE
                    //hpd
                    //binding.vehicleDetails.operatingHrPerDay.visibility = View.VISIBLE
                    //dpm
                    //binding.vehicleDetails.perMonthTv.visibility = View.VISIBLE
                    //
                    //binding.vehicleDetails.perMonthTv.text = "${_transaction?.costData?.dpm?.toInt()} days"

                    //operating days and no. of days in a week
                    //for "intracity" - no. of days a week
                    //for everything else - operating days
                    //binding.vehicleDetails.operationalDaysText.visibility = View.VISIBLE
                    //binding.vehicleDetails.operatingDays.visibility = View.VISIBLE
                    //handle visibility of "operatingDays" textview and its label if response is null


                    if(_transaction.isIntracity()){
                        //handle visibility of "operatingDays" textview and its label if response is null OR zero
                        if(_transaction.costData?.kpm == null || _transaction.costData.kpm.toInt() == 0){
                            binding.vehicleDetails.operatingDays.visibility = View.GONE
                            binding.vehicleDetails.operationalDaysText.visibility = View.GONE
                            binding.vehicleDetails.operationalDaysIcon.visibility = View.GONE
                        }

                        //for "intracity" - no. of days a week
                        binding.vehicleDetails.operatingDays.text = "~ ${_transaction.costData?.kpm?.toInt()} kms"
                    }else{
                        //handle visibility of "operatingDays" textview and its label if response is null OR zero
                        if(_transaction.routeInfo?.routeDaysOfWeek == null || _transaction.routeInfo.routeDaysOfWeek.size == 0){
                            binding.vehicleDetails.operatingDays.visibility = View.GONE
                            binding.vehicleDetails.operationalDaysText.visibility = View.GONE
                            binding.vehicleDetails.operationalDaysIcon.visibility = View.GONE
                        }

                        //for everything else - operating days
                        binding.vehicleDetails.operatingDays.text = "${_transaction.routeInfo?.routeDaysOfWeek?.size} days a week"
                    }



                    //insert payment information section


                    //insert guidelines section

                    //

                    //Insert code above this
                }
                binding.executePendingBindings()
            } else {
                binding.error = true
                binding.containerError.title = "Session Time Out"
                binding.containerError.subTitle =
                    "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
                binding.containerError.actionLabel = "REFRESH"
            }
        }
    }

    private fun setupRouteAdapter() {
        val contractsRouteDetailsAdapter  = PlacementsContractsRouteDetailsAdapter(routesArray, viewModel.transaction,this@PlacementsContractDetailsActivity)
        binding.routeDetails.rvContracts.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = contractsRouteDetailsAdapter
        }
    }


    /**
     * data class Center(
     *     @SerializedName("center_code") val centerCode: String?,
     *     @SerializedName("center_name") val centerName: String?,
     *     @SerializedName("center_state") val centerState: String?,
     *     @SerializedName("coordinates") val coordinates: Coordinates?,
     *     @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
     *     @SerializedName("rel_eta") val relETA: String?,
     *     @SerializedName("rel_etd") val relETD: String?
     * )
     */

    private fun prepareRoutesData() {
        var pickupCenter : HaltCenters? =null
        var dropCenter : HaltCenters? =null
        var haltCenter : HaltCenters? = HaltCenters(
            relEtd = "",
            relEta = "",
            name = "",
            state = "",
            pastTravelHrs = "",
            haltHrs = "",
            longitude = "",
            latitude = "")
        var haltCenters : List<HaltCenters> = ArrayList()

        pickupCenter = HaltCenters(
            relEtd = viewModel.transaction.routeInfo?.origin?.relETD?:"",
            relEta = viewModel.transaction.routeInfo?.origin?.relETA?:"",
            name = viewModel.transaction.routeInfo?.origin?.centerName?:"",
            state = viewModel.transaction.routeInfo?.origin?.centerState?:"",
            pastTravelHrs = "",
            haltHrs = "",
            longitude = viewModel.transaction.routeInfo?.origin?.coordinates?.lon?.toString()?:"",
            latitude = viewModel.transaction.routeInfo?.origin?.coordinates?.lat?.toString()?:"")

        dropCenter = HaltCenters(
            relEtd = viewModel.transaction.routeInfo?.destination?.relETD?:"",
            relEta = viewModel.transaction.routeInfo?.destination?.relETA?:"",
            name = viewModel.transaction.routeInfo?.destination?.centerName?:"",
            state = viewModel.transaction.routeInfo?.destination?.centerState?:"",
            pastTravelHrs = "",
            haltHrs = "",
            longitude = viewModel.transaction.routeInfo?.destination?.coordinates?.lon?.toString()?:"",
            latitude = viewModel.transaction.routeInfo?.destination?.coordinates?.lat?.toString()?:"")

        //add pickup into routes array
        routesArray.clear()
        routesArray.add(pickupCenter)
        //
        //sort halt centers based on position in ascending order and keeping all the items with null as "position" in the end of the sorted list.
        val sortedList = viewModel.transaction.routeInfo?.haltCenters?.sortedWith(compareBy(nullsLast()) { it.position })
        //
        sortedList?.forEach({
            haltCenter = HaltCenters(
                relEtd = it.relETD,
                relEta = it.relETA?:"",
                name = it.centerName?:"",
                state = it.centerState?:"",
                pastTravelHrs = it.pastTravelHrs?.toString()?:"",
                haltHrs = it.haltHours?.toString()?:"",
                longitude = it.coordinates?.lon?.toString()?:"",
                latitude = it.coordinates?.lat?.toString()?:"")

            haltCenter?.let { innerIt ->
                routesArray.add(innerIt)
            }
        })

        //add drop into routes array
        routesArray.add(dropCenter)

        //Log.d("routesArray===>>>", Gson().toJson(routesArray))
    }

    /**
     * data class HaltCenters(
     *     @SerializedName("center_code") val centerCode: String?,
     *     @SerializedName("center_name") val centerName: String?,
     *     @SerializedName("center_state") val centerState: String?,
     *     @SerializedName("coordinates") val coordinates: Coordinates?,
     *     //
     *     @SerializedName("halt_hrs") val haltHours: Int?,
     *     @SerializedName("position") val position: Int?,
     *     @SerializedName("address") val address: String?,
     *     @SerializedName("city") val city: String?,
     *     //
     *     @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
     *     @SerializedName("rel_eta") val relETA: String?,
     *     @SerializedName("rel_etd") val relETD: String?
     * )
     */

    private fun triggerFacilityAddress(originCenterCode:String?){
        //
        binding.cardInput.pbIntracityAddress.visibility = View.VISIBLE
        //
        viewModel.getFacilityAddress(originCenterCode)
    }


    fun placementInput(){
        Log.d(TAG, "placementInput-START")
        //
        //binding.cardInput.placementCl.editAutoCompleteTrucks.visibility =  View.GONE
        binding.cardInput.placementCl.driverNameError.visibility = View.GONE
        //
        //insert your code below this
        //complete if block
        if (viewModel.transaction.isIntracity()) {
            Log.d(TAG, "if-isIntracity-START")
            //
            viewModel.addressLiveData.removeObservers(this@PlacementsContractDetailsActivity)
            viewModel.addressLiveData.observe(this, Observer {
                //set reporting city
                it?.let {
                    viewModel.propertyAddressData = it
                }
                //set full property address
                it?.propertyAddressDetails?.address?.let { address ->
                    binding.cardInput.pbIntracityAddress.visibility = View.GONE
                    binding.cardInput.routeAddress.text = capitalize(address)
                    Log.d(TAG, "if-addressLiveData-START")
                }
            })
            //fetch facility address
            triggerFacilityAddress(homePlacementsItemData?.originCenterCode)
            //set visible intracity address view
            binding.cardInput.routeAddress.visibility = View.VISIBLE
            ////set visible map text view
            binding.cardInput.mapText.visibility = View.VISIBLE
            //set click listener on map
            binding.cardInput.mapText.setOnClickListener {
                navigateToMap()
            }
        }else{
            binding.cardInput.pbIntracityAddress.visibility = View.GONE
            binding.cardInput.routeAddress.visibility = View.GONE
            binding.cardInput.mapText.visibility = View.GONE
        }
        //




        //insert your code above this


        //old working code below done by Rahul - no change

        //
        viewModel.updateVehicleDetails.removeObservers(this)
        viewModel.updateVehicleDetails.observe(this, Observer {
            if (it != null && it == true) {
                REFRESH_ON_BACK_PLACEMENT = true
                uiUtils.hideProgress()
                // pushMoengageEvent(true)
                //show newly added success dialog incase of adhoc intracity only
                when(homePlacementsItemData?.loadType){

                    LoadTypes.intracityAdhoc.name -> showIntracityAdhocSuccessDialog()

                    else -> showSuccessEditDialog("Submitted successfully!")
                }

            }else{
                uiUtils.hideProgress()
            }
        })
        //
        autoCompleteUtils.autoCompleteTruck(binding.cardInput.placementCl.editAutoCompleteTrucks){
            // Ensure UI operations run on main thread
            binding.cardInput.placementCl.editAutoCompleteTrucks.post {
                if(it=="Add New Truck"){
                    // Existing input is preserved by DelhiveryTrucksAutoEditText
                    this.let {showAddTruckBottomSheet()}
                }else if(validateTruckNumber(it)){
                    isValidVehicleNumber = true
                    binding.cardInput.placementCl.vehicleError.visibility = View.GONE
                    binding.cardInput.placementCl.editTextVehicleNumber.text = it
                    binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
                    binding.cardInput.placementCl.editTextVehicleNumber.visibility =  View.VISIBLE
                    enableSubmitPlacement()
                } else {
                    binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
                    binding.cardInput.placementCl.vehicleError.text ="Please enter valid vehicle Number"
                    isValidVehicleNumber = false
                    enableSubmitPlacement()
                }
            }
        }
        //
        binding.cardInput.placementCl.editTextVehicleNumber.setOnClickListener {
            binding.cardInput.placementCl.editTextVehicleNumber.visibility =  View.GONE
            //
            binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.VISIBLE
            //
            binding.cardInput.placementCl.editAutoCompleteTrucks.focusClick()
            //
            //move cursor to the end of text in vehicle number
//            binding.cardInput.placementCl.editTextVehicleNumber.text.trim().length.let {
//                if(it>0)
//                    binding.cardInput.placementCl.editAutoCompleteTrucks.setSelection(it)
//            }
            //
            Handler().postDelayed({
                showKeyboard()
            }, 100)
            //
            //binding.cardInput.placementCl.editAutoCompleteTrucks.requestFocus()
            //
            isValidVehicleNumber = false
            //
            enableSubmitPlacement()
        }


        // Add text change listener for vehicle number to trigger driver data loading
        binding.cardInput.placementCl.editTextVehicleNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Trigger driver data loading when vehicle number changes
                // The autoCompleteDriverNameWithPhone will handle this automatically
                enableSubmitPlacement()
            }
        })

        // Driver name field click handling - no need to force focus
        binding.cardInput.placementCl.editAutoCompleteDriverName.setOnClickListener {
            Log.d(TAG,"editAutoCompleteDriverName-LISTENER")
            hasUserInteractedWithDriverName = true
            //binding.cardInput.placementCl.editAutoCompleteDriverName.requestFocus()
            // Don't call enableSubmitPlacement here - let TextWatcher handle it
        }

        // Simple TextWatcher for manual typing validation
        binding.cardInput.placementCl.editAutoCompleteDriverName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (text.length >= 2) {
                    isValidDriverName = true
                    binding.cardInput.placementCl.driverNameError.visibility = View.GONE
                } else {
                    isValidDriverName = false
                    if (hasUserInteractedWithDriverName && text.isNotEmpty()) {
                        binding.cardInput.placementCl.driverNameError.visibility = View.VISIBLE
                        binding.cardInput.placementCl.driverNameError.text = "Please enter valid driver name"
                    } else {
                        binding.cardInput.placementCl.driverNameError.visibility = View.GONE
                    }
                }
                enableSubmitPlacement()
            }
        })

        // Text change handling is now managed by the AutoCompleteUtils
        homePlacementsItemData?.vehicleNumber?.let {
            Log.i("vehicleNumber",it)
            isValidVehicleNumber = true
            binding.cardInput.placementCl.editTextVehicleNumber.text = it
            binding.cardInput.placementCl.editAutoCompleteTrucks.setText(it)
            binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
            binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
        }
        //
        homePlacementsItemData?.driverName?.let {
            Log.i("DriverNameValidation", "Initial setup: Found existing driver name: '${it}'")
            isValidDriverName = true
            binding.cardInput.placementCl.editAutoCompleteDriverName.setText(it)
            binding.cardInput.placementCl.driverNameError.visibility = View.GONE
        }
        //
        homePlacementsItemData?.driverPhone?.let {
            isValidDriverNumber = true
            val trimmedNumber = it?.replace("+91", "")?.replaceFirst("^0+".toRegex(), "")?.trim()
            binding.cardInput.placementCl.editDriverNumber.setText(trimmedNumber)
        }

        // Call enableSubmitPlacement after initial data setup
        enableSubmitPlacement()
        //
        if (homePlacementsItemData?.status=="Marked-in"){
            binding.cardInput.placementCl.editTextVehicleNumber.isEnabled = false
            binding.cardInput.placementCl.editAutoCompleteDriverName.isEnabled = false
            binding.cardInput.placementCl.editDriverNumber.isEnabled = false
            binding.cardInput.placementCl.btnContactPicker.isEnabled = false
            disableSubmitPlcButton()
        }
        //
        binding.cardInput.placementCl.btnSubmit.setOnClickListener{
            submitPlacementDetails()
        }

        // Simple driver name autocomplete
        autoCompleteUtils.autoCompleteDriverNameWithPhone(
            binding.cardInput.placementCl.editAutoCompleteDriverName,
            { binding.cardInput.placementCl.editTextVehicleNumber.text.toString() }
        ){ driverData ->

            driverData.driverName?.let {
                isValidDriverName = it.length>=2
            }
            // Populate phone number when driver is selected from dropdown
            driverData.driverPhone?.let {
                binding.cardInput.placementCl.editDriverNumber.setText(it)
                isValidDriverNumber = validatePhoneNumber(it)
            }
            enableSubmitPlacement()
        }


        binding.cardInput.placementCl.editDriverNumber.addTextChangedListener(object : TextWatcher {
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
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    val input = s.trim()
                        .toString()
                    if(input.length==10 && validatePhoneNumber(input)){
                        isValidDriverNumber = true
                        // binding.cardInput.placementCl.driverNumberError.visibility = View.GONE
                    } else {
                        //  binding.cardInput.placementCl.driverNumberError.visibility = View.VISIBLE
                        //  binding.cardInput.placementCl.driverNumberError.text ="Please enter a valid driver number"
                        isValidDriverNumber = false

                    }
                    enableSubmitPlacement()

                }else{
                    isValidDriverNumber = false
                    enableSubmitPlacement()

                }
            }
        })

        // Contact picker functionality
        binding.cardInput.placementCl.btnContactPicker.setOnClickListener {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(pickContactIntent, REQCODE_PICK_CONTACT)
        }
    }

    private fun showKeyboard() {

        binding.cardInput.placementCl.editAutoCompleteTrucks.requestFocus()

        // Delay to ensure the window is ready before showing the keyboard
        binding.cardInput.placementCl.editAutoCompleteTrucks.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.cardInput.placementCl.editAutoCompleteTrucks, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun showAddTruckBottomSheet() {
        val dialog = AddTruckBottomSheetDialogFragment.newInstance(
            truckNumber= binding.cardInput.placementCl.editAutoCompleteTrucks.text?.toString()?:"",
            viewModelFactory,
            userPrefs,
            autoCompleteUtils,
            onTruckAdded = { truckNumber ->
                showSuccessEditDialog("Truck added successfully!")
                // Populate the vehicle number field with the newly added truck
                binding.cardInput.placementCl.editTextVehicleNumber.text = truckNumber
                binding.cardInput.placementCl.editAutoCompleteTrucks.setText(truckNumber)
                binding.cardInput.placementCl.editAutoCompleteTrucks.visibility = View.GONE
                binding.cardInput.placementCl.editTextVehicleNumber.visibility = View.VISIBLE
                isValidVehicleNumber = true
                enableSubmitPlacement()
            }
        )
        dialog.show(supportFragmentManager, "AddTruckBottomSheetDialogFragment")
    }
    private fun  enableSubmitPlacement(){
        // DEBOUNCE PROTECTION - Prevent rapid calls to enableSubmitPlacement (reduced from 500ms to 100ms)
        /* val currentTime = System.currentTimeMillis()
         val timeSinceLastCall = currentTime - lastEnableSubmitTime
         if (timeSinceLastCall < 100) { // Less than 100ms since last call
           Log.i("validate", "Skipping enableSubmitPlacement - too frequent (${timeSinceLastCall}ms since last)")
           return
         }
         lastEnableSubmitTime = currentTime*/

        // Re-validate all conditions
        isValidVehicleNumber = validateTruckNumber(binding.cardInput.placementCl.editTextVehicleNumber.text.toString())

        // Re-validate driver name
        val driverNameText = binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString()
        isValidDriverName = driverNameText.length >= 2

        // Re-validate driver phone
        val driverPhoneText = binding.cardInput.placementCl.editDriverNumber.text.toString()
        isValidDriverNumber = driverPhoneText.length == 10 && validatePhoneNumber(driverPhoneText)

        val errorVisibility = if(binding.cardInput.placementCl.driverNameError.visibility == View.VISIBLE) "VISIBLE" else "GONE"
        Log.i("validate", "DriverName: '$driverNameText', length: ${driverNameText.length}, isValidDriverName: $isValidDriverName, isValidVehicleNumber: $isValidVehicleNumber, isValidDriverNumber: $isValidDriverNumber, hasUserInteracted: $hasUserInteractedWithDriverName, errorVisibility: $errorVisibility")

        val isMarkedIn = homePlacementsItemData?.status == "Marked-in"
        val allValid = isValidDriverName && isValidVehicleNumber && isValidDriverNumber && !isMarkedIn

        Log.i("validate", "All conditions: driverName=$isValidDriverName, vehicle=$isValidVehicleNumber, phone=$isValidDriverNumber, markedIn=$isMarkedIn, allValid=$allValid")

        if(allValid){
            enableSubmitPlcButton()
        }else{
            disableSubmitPlcButton()
        }
    }
    private fun disableSubmitPlcButton(){
        binding.cardInput.placementCl.btnSubmit.isEnabled = false
    }
    private fun enableSubmitPlcButton(){
        binding.cardInput.placementCl.btnSubmit.isEnabled = true
    }

    private fun validatePhoneNumber(number:String):Boolean{
        val sameDigitsReg = "^([0-9])\\1*$"
        var result = false
        val sameDigitsPattern = Pattern.compile(
            sameDigitsReg
        )
        if(!sameDigitsPattern.matcher(number).matches()){
            val standardNumberPattern = Pattern.compile(
                "^[6-9]{1}[0-9]{9}$"
            )
            result = standardNumberPattern.matcher(number).matches()
        }

        return result
    }
    private fun submitPlacementDetails() {
        if (binding.cardInput.placementCl.editAutoCompleteTrucks.isVisible) {
            binding.cardInput.placementCl.vehicleError.visibility = View.VISIBLE
            binding.cardInput.placementCl.vehicleError.text = "Please select a valid vehicle number"
            isValidVehicleNumber = false
            binding.cardInput.placementCl.editAutoCompleteTrucks.errorAnimate()
            // Don't call enableSubmitPlacement here - let TextWatcher handle it
        } else {
            binding.cardInput.placementCl.vehicleError.visibility = View.GONE

            try {
                val isOrionType =  homePlacementsItemData?.loadType!!.toLowerCase().contains("orion")

                val vehicleType = if (isOrionType ) {null} else if (homePlacementsItemData?.loadType!!.toLowerCase().contains("adhoc")) {
                    "adhoc"
                } else {
                    "regular"
                }
                val contractType = if (isOrionType ) {null} else if  (homePlacementsItemData?.loadType!!.toLowerCase().contains("ftl")) {
                    "ftl"
                } else {
                    "intracity"
                }
                val action = if(isOrionType) {
                    "placement_on_orion"
                } else if (homePlacementsItemData?.vehicleNumber == null) {
                    "add_placement"
                } else {
                    "update_placement"

                }
                try {
                    uiUtils.showProgress()
                    //  pushMoengageEvent(false)
                    val updateVehicleDetailsRequest = UpdateVehicleDetailsRequest(
                        binding.cardInput.placementCl.editTextVehicleNumber.text.toString(),
                        binding.cardInput.placementCl.editAutoCompleteDriverName.text.toString(),
                        binding.cardInput.placementCl.editDriverNumber.text.toString(),
                        contractType,
                        vehicleType,
                        homePlacementsItemData?.vehicleType!!,
                        homePlacementsItemData?.transporterSupplierId!!,
                        contractId = if (LoadTypeUtils.isContractCodeRequired(homePlacementsItemData?.loadType?:"")) homePlacementsItemData?.contractId else null,
                        homePlacementsItemData?.transporterId!!,
                        action,
                        if(isOrionType) null else homePlacementsItemData?.reportingTime!!,
                        if(isOrionType)null else homePlacementsItemData?.originCenterCode!!,
                        if(isOrionType)null else homePlacementsItemData?.vehicleNumber,
                        if(isOrionType)null else homePlacementsItemData?.vehicleId,
                        if(isOrionType)null else homePlacementsItemData?.driverName,
                        if(isOrionType)null else homePlacementsItemData?.driverPhone,
                        transactionId = if (LoadTypeUtils.isTransactionIdRequired(homePlacementsItemData?.loadType?:"")) homePlacementsItemData?.transactionId else null
                    )
                    viewModel.updateVehicleDetails(updateVehicleDetailsRequest)
                }catch (e:Exception){
                    uiUtils.showToast("Something went wrong")
                    uiUtils.hideProgress()
                }
            } catch (e: IllegalArgumentException) {
                uiUtils.hideProgress()
                Log.e("AcceptAdhocBid", e.toString())
            }
        }
    }
    override fun getViewModelClass()= ContractDetailsViewModel::class.java

    override fun layoutId(): Int= R.layout.activity_placements_contract_details
    override fun requireConnection(): Boolean = true

    private fun fetchData() {
        binding.error = false
        //fetch placement details data
        viewModel.fetchPlacementDetails()
        //viewModel.fetchPlacementDetailsLocal(mContext = applicationContext)
        //execuite pending bundings
        binding.executePendingBindings()
        //
        viewModel.addressLiveData.value = null
        //
        viewModel.propertyAddressData = null
        //
        binding.cardInput.routeAddress.text = ""
        //clear arrays
        routesArray.clear()
        //
        viewModel.updateVehicleDetails.value = null
    }




    fun navigateToBid(dialog: Dialog){
        var dialogCancelled = false
        dialog.setOnCancelListener {
            dialogCancelled = true
        }

        dialog.setOnDismissListener {
            dialogCancelled = true
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }

            if (!dialogCancelled && !isFinishing && !isDestroyed) {
                dialog.dismiss()
                startActivity(homeActivityIntent("load", this@PlacementsContractDetailsActivity))
                finish()
            }
        }, 5000)
    }

    private fun showIntracityAdhocSuccessDialog(){
        // prepare dialog UI's and whatsapp share data
        val title = getString(string.title_dialog_success)
        val subTittle = getString(string.sub_title_dialog_success)
        val playStoreLink = getString(string.driver_app_link)
        val hindiVideoLink = getString(string.hindi_video_link)
        val englishVideoLink = getString(string.english_video_link)

        // Extract data from the placement item
        val placementData = homePlacementsItemData
        Log.d("placementData=====>>>>", Gson().toJson(placementData))
        val ticketId = placementData?.transactionId ?: ""
        val reportingCentre = generateReportingCentreLink(placementData!!)
        val reportingTime = formatReportingTime(placementData?.reportingTime)
        //val hindiVideoLink = "https://youtube.com/watch?v=hindi_video_id"
        //val englishVideoLink = "https://youtube.com/watch?v=english_video_id"

        // Show the dialog
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
                    // Handle dialog dismissal if needed
//          Log.d("onDialogDismissed===>>>","Dialog dismissed")
//          Handler(Looper.myLooper()!!).postDelayed({
//            REFRESH_ON_BACK_PLACEMENT = true
//            finish()
//          }, 100)
                }
            }
        )
    }

    /**
     * Generate Google Maps link for reporting centre based on placement data
     */
    private fun generateReportingCentreLink(placementData: HomePlacementsItemData): String {
        return if (placementData.originCenterLat != null && placementData.originCenterLong != null) {
            // Use GPS coordinates if available
            "https://maps.google.com/?q=${placementData.originCenterLat},${placementData.originCenterLong}"
        } else {
            // Fallback to origin center name
            val location = placementData.originCenterName ?: ""
            if (location.isNotEmpty()) {
                "https://maps.google.com/?q=$location"
            } else {
                ""
            }
        }
    }

    private fun navigateToMap(){
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${homePlacementsItemData?.originCenterLat},${homePlacementsItemData?.originCenterLong}"+"(" + homePlacementsItemData?.originCenterName+ ")")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open map", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Format reporting time from placement data
     */
    private fun formatReportingTime(reportingTime: String?): String {
        return if (!reportingTime.isNullOrEmpty()) {
            try {
                // Parse the time and format it
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                val outputFormat = SimpleDateFormat("dd MMM, hh:mm a")
                val date = inputFormat.parse(reportingTime)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "--:--" // Default time if parsing fails
            }
        } else {
            "--:--" // Default time if no reporting time available
        }
    }

    private fun showSuccessEditDialog(msg:String){

        val dialog = Dialog(this)
        val bindingDialog= DialogPlacementDetailsEditBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.titleText.text = msg
        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }
        if(!this.isFinishing)
            dialog.show()
        /*  if(!this.isFinishing)
            Handler(Looper.myLooper()!!).postDelayed({
              dialog.dismiss()
              REFRESH_ON_BACK_PLACEMENT = true
              finish()
            }, 2000)*/
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    override fun bidPlacedSuccess(success: Boolean) {
        if(success)
            startActivity(homeActivityIntent("load", this@PlacementsContractDetailsActivity))

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
                                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex != -1) {
                                    val phoneNumber = c.getString(numberIndex)
                                    Log.d("phoneNumber==>>>",phoneNumber)
                                    var trimmedNumber = phoneNumber?.replace("+91", "")?.replaceFirst("^0+".toRegex(), "")?.trim()
                                    trimmedNumber = trimmedNumber?.replace(" ", "")?.trim()
                                    Log.d("trimmedNumber==>>>",trimmedNumber?:"")
                                    binding.cardInput.placementCl.editDriverNumber.setText(trimmedNumber)
                                    enableSubmitPlacement()
                                }
                            }
                        }
                    }
                }
            }
        }
    }



}

/* intent keys */
private const val PlacementTypeIndentKey = "placementType"
private const val TransactionIdIntentKey = "transaction_id"
private const val ContractCodeIntentKey = "contractCode"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
/**
 * Bid details intent
 */
private const val ForPlacementKey = "for_placement"

private const val PlacementData = "placement_data"


fun placementsContractDetailsIntent(
    placementType:String,
    transactionId: String?,
    contractCode: String?,
    context: Context,
    source: String?= VALUE_APP_FLOW,
    forPlacement:Boolean=false,
    homePlacementsItemData: HomePlacementsItemData?=null
) = Intent(context, PlacementsContractDetailsActivity::class.java).apply {
    if (placementType.isNotNullOrEmpty()) putExtra(PlacementTypeIndentKey, placementType)
    if (transactionId.isNotNullOrEmpty()) putExtra(TransactionIdIntentKey, transactionId)
    if (contractCode.isNotNullOrEmpty()) putExtra(ContractCodeIntentKey, contractCode)
    putExtra(PROPERTY_SOURCE,source)
    putExtra(ForPlacementKey, forPlacement)
    putExtra(PlacementData, homePlacementsItemData)

}
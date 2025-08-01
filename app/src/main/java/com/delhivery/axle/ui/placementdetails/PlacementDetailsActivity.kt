package com.delhivery.axle.ui.placementdetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.data.home.placements.HOME_PLACEMENT_ITEM_DATA
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.databinding.ActivityPlacementsDetailsBinding
import com.delhivery.axle.databinding.DialogKycSubmittedBinding
import com.delhivery.axle.databinding.DialogPlacementDetailsEditBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DetailsSubmittedSuccessInterface
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_ADD_DETAILS_ATTEMPTED
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_ADD_DETAILS_SUCCESS
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_EDIT_DETAILS_ATTEMPTED
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_EDIT_DETAILS_SUCCESS
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_DRIVER_NAME
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_DRIVER_NUMBER
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_SUBMIT
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_VEHICLE_NUMBER
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_DRIVER_NAME
import com.delhivery.axle.utils.PROPERTY_DRIVER_NUMBER
import com.delhivery.axle.utils.PROPERTY_DRIVER_PHONE
import com.delhivery.axle.utils.PROPERTY_EXPECTED_TIME
import com.delhivery.axle.utils.PROPERTY_MISSING_FLAG
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TIMESTAMP
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PROPERTY_VEHICLE_NO
import com.delhivery.axle.utils.PROPERTY_VEHICLE_NUMBER
import com.delhivery.axle.utils.REQCODE_ADD_TRUCK
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_PAGE
import com.delhivery.axle.utils.VALUE_ADD_TRUCK_PLACEMENT
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.getSerializableExtra
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.time.LocalDateTime
import java.util.Date
import java.util.regex.Pattern
import javax.inject.Inject


class PlacementDetailsActivity: BaseActivity<ActivityPlacementsDetailsBinding, PlacementDetailsViewModel>() {

    private var isValidVehicleNumber = false
    private var isValidDriverNumber = false
    private var isValidDriverName = false
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    @Inject
    lateinit var autoCompleteUtils: AutoCompleteUtils
    @Inject
    lateinit var userPrefs: UserPrefs
    override fun getViewModelClass()= PlacementDetailsViewModel::class.java

    override fun layoutId(): Int= R.layout.activity_placements_details

    override fun requireConnection(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("PlacementDetailsActivity_SetupTime")
        activitySetupTrace?.start()
        try {
            require(
                !(intent == null || !intent.hasExtra(HOME_PLACEMENT_ITEM_DATA))
            ) { "Required data $HOME_PLACEMENT_ITEM_DATA not found" }
        } catch (e: Exception) {
            finish()
        }
        if( intent?.extras?.getSerializableExtra(HOME_PLACEMENT_ITEM_DATA, HomePlacementsItemData::class.java)!=null)
            viewModel.homePlacementsItemData =
                intent.extras?.getSerializableExtra(HOME_PLACEMENT_ITEM_DATA,HomePlacementsItemData::class.java)!!

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.homePlacementsItemData.detailVisible = true
        viewModel.getFacilityAddress()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Vehicle Details"
        /* setup live data observers */
       // viewModel.getAllInventories("MH")\
        binding.editAutoCompleteTrucks.visibility =  View.GONE

        autoCompleteUtils.autoCompleteTruck(binding.editAutoCompleteTrucks){
            if(it=="Add New Truck"){
                binding.editAutoCompleteTrucks.text.clear()
                this?.let { startActivityForResult(truckIntent(this, source = VALUE_ADD_TRUCK_PLACEMENT), REQCODE_ADD_TRUCK) }
            }else if(validateTruckNumber(it)){
                        isValidVehicleNumber = true
                        binding.vehicleNumberError.visibility = View.GONE
                        binding.editTextVehicleNumber.setText(it)
                        binding.editAutoCompleteTrucks.visibility = View.GONE
                        binding.editTextVehicleNumber.visibility =  View.VISIBLE
                        enableSubmit()
                    } else {
                        binding.vehicleNumberError.visibility = View.VISIBLE
                        binding.vehicleNumberError.text ="Please enter valid vehicle Number"
                        isValidVehicleNumber = false
                        enableSubmit()
                    }
        }

        binding.editTextVehicleNumber.setOnClickListener {
            binding.editTextVehicleNumber.visibility =  View.GONE
            binding.editAutoCompleteTrucks.visibility = View.VISIBLE
            binding.editAutoCompleteTrucks.focusClick()
            isValidVehicleNumber = false
            enableSubmit()
        }

        viewModel.homePlacementsItemData.vehicleNumber?.let {
            isValidVehicleNumber = true
            binding.editTextVehicleNumber.setText(it)
            enableSubmit()
        }
        viewModel.homePlacementsItemData.driverName?.let {
            isValidDriverName = true
            binding.editTextDriverName.setText(it)
            enableSubmit()
        }
        viewModel.homePlacementsItemData.driverPhone?.let {
            isValidDriverNumber = true
            binding.editTextDriverNumber.setText(it)
            enableSubmit()
        }
        when(viewModel.homePlacementsItemData.loadType){
            LoadTypes.intracityAdhoc.name -> {
                binding.intracityAdhoc.root.visibility = View.VISIBLE
                binding.intracityAdhoc.request = viewModel.homePlacementsItemData
                binding.intracityAdhoc.mapText.setOnClickListener{
                    navigateToMap()
                }
            }
            LoadTypes.intracityRegular.name ->{
                binding.intracityContract.root.visibility = View.VISIBLE
                binding.intracityContract.request = viewModel.homePlacementsItemData
                binding.intracityContract.mapText.setOnClickListener{
                    navigateToMap()
                }
            }
            LoadTypes.ftlAdhoc.name ->{
                binding.intercityAdhoc.root.visibility = View.VISIBLE
                binding.intercityAdhoc.request = viewModel.homePlacementsItemData
                binding.intercityAdhoc.originDestination.request = viewModel.homePlacementsItemData
                binding.intercityAdhoc.mapText.setOnClickListener{
                    navigateToMap()
                }
            }
            LoadTypes.ftlRegular.name -> {
                binding.intercityContract.root.visibility = View.VISIBLE
                binding.intercityContract.request = viewModel.homePlacementsItemData
                binding.intercityContract.originDestination.request = viewModel.homePlacementsItemData
                binding.intercityContract.mapText.setOnClickListener{
                    navigateToMap()
                }
            }
        }
        if (viewModel.homePlacementsItemData.status=="Marked-in"){
            binding.editTextVehicleNumber.isEnabled = false
            binding.editTextDriverName.isEnabled = false
            binding.editTextDriverNumber.isEnabled = false
            disableSubmitButton()
        }
        viewModel.addressLiveData.observe(this, Observer {
            binding.intracityAdhoc.originCenterAddress.text = it.propertyAddressDetails?.address
            binding.intracityContract.originCenterAddress.text = it.propertyAddressDetails?.address

        })


        viewModel.updateVehicleDetails.observe(this, Observer {
         if(it){
             uiUtils.hideProgress()
             pushMoengageEvent(true)
             //show newly added success dialog incase of adhoc intracity only
             when(viewModel.homePlacementsItemData.loadType){
                 LoadTypes.intracityAdhoc.name -> showIntracityAdhocSuccessDialog()
                 else -> showSuccessEditDialog()
             }

         }else{
             uiUtils.hideProgress()
         }
        })

        binding.btnSubmit.setOnClickListener{
            submit()
        }

        binding.editTextDriverName?.addTextChangedListener(object : TextWatcher {
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
                    if(input.length>=2){
                        isValidDriverName = true
                        binding.driverNameError.visibility = View.GONE
                    } else {
                        binding.driverNameError.visibility = View.VISIBLE
                        binding.driverNameError.text ="Please enter valid driver name"
                        isValidDriverName = false

                    }
                    enableSubmit()

                }else{

                    isValidDriverName = false
                    enableSubmit()

                }
            }
        })

        binding.editTextDriverNumber?.addTextChangedListener(object : TextWatcher {
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
                        binding.driverNumberError.visibility = View.GONE
                    } else {
                        binding.driverNumberError.visibility = View.VISIBLE
                        binding.driverNumberError.text ="Please enter a valid driver number"
                        isValidDriverNumber = false

                    }
                    enableSubmit()

                }else{
                    isValidDriverNumber = false
                    enableSubmit()

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun  enableSubmit(){
        isValidVehicleNumber = validateTruckNumber(binding.editTextVehicleNumber.text.toString())
        if(isValidDriverName&&isValidVehicleNumber&&isValidDriverNumber){
            enableSubmitButton()
        }else{
            disableSubmitButton()
        }
    }
    private fun disableSubmitButton(){
        binding.btnSubmit.isEnabled = false
    }
    private fun enableSubmitButton(){
        binding.btnSubmit.isEnabled = true
    }
    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
            "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
        )
        return pattern.matcher(number).matches()
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
    private fun submit() {
        if (binding.editAutoCompleteTrucks.visibility == View.VISIBLE) {
            binding.vehicleNumberError.visibility = View.VISIBLE
            binding.vehicleNumberError.text = "Please select a valid vehicle number"
            isValidVehicleNumber = false
            binding.editAutoCompleteTrucks.errorAnimate()
            enableSubmit()
        } else {
            binding.vehicleNumberError.visibility = View.GONE

        try {
            val vehicleType = if (viewModel.homePlacementsItemData.loadType!!.toLowerCase().contains("adhoc")) {
                "adhoc"
            } else {
                "regular"
            }
            val contractType = if (viewModel.homePlacementsItemData.loadType!!.toLowerCase().contains("ftl")) {
                "ftl"
            } else {
                "intracity"
            }
            val action = if (viewModel.homePlacementsItemData.vehicleNumber == null) {
                "add_placement"
            } else {
                "update_placement"

            }
            try {
                uiUtils.showProgress()
                pushMoengageEvent(false)
                val updateVehicleDetailsRequest = UpdateVehicleDetailsRequest(binding.editTextVehicleNumber.text.toString(), binding.editTextDriverName.text.toString(), binding.editTextDriverNumber.text.toString(), contractType, vehicleType, viewModel.homePlacementsItemData.vehicleType!!, viewModel.homePlacementsItemData.transporterSupplierId!!, viewModel.homePlacementsItemData.contractId, viewModel.homePlacementsItemData.transporterId!!, action, viewModel.homePlacementsItemData.reportingTime!!, viewModel.homePlacementsItemData.originCenterCode!!, viewModel.homePlacementsItemData.vehicleNumber, viewModel.homePlacementsItemData.vehicleId, viewModel.homePlacementsItemData.driverName, viewModel.homePlacementsItemData.driverPhone,viewModel.homePlacementsItemData.transactionId)
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

    private fun pushMoengageEvent(success:Boolean){
        var eventName = ""
        if(viewModel.homePlacementsItemData.vehicleNumber !=null) {
            eventName = if(success){
                EVENT_HOME_PLACEMENT_EDIT_DETAILS_SUCCESS
            }else{
                EVENT_HOME_PLACEMENT_EDIT_DETAILS_ATTEMPTED
            }
        }else{
            eventName = if(success){
                EVENT_HOME_PLACEMENT_ADD_DETAILS_SUCCESS
            }else{
                EVENT_HOME_PLACEMENT_ADD_DETAILS_ATTEMPTED
            }
        }
            analyticsUtil.moEngageTrackEvent(
                    eventName,
                    mutableListOf(
                            PROPERTY_USER_ID,
                            PROPERTY_PHONE_NO,
                            PROPERTY_DEMAND_TYPE,
                            PROPERTY_TIMESTAMP,
                            PROPERTY_VEHICLE_NO,
                            PROPERTY_DRIVER_PHONE,
                            PROPERTY_DRIVER_NAME,
                            PROPERTY_EXPECTED_TIME
                    ),
                    mutableListOf(
                            userPrefs.userId(),
                            userPrefs.phoneNumber?:"",
                            viewModel.homePlacementsItemData.loadType?:"",
                            DateUtils.presentDay()+" "+DateUtils.presentTime(),
                            binding.editTextVehicleNumber.text.toString(),
                            binding.editTextDriverNumber.text.toString(),
                            binding.editTextDriverName.text.toString(),
                            viewModel.homePlacementsItemData.reportingTime.toString()
                    )
            )

    }
    private fun navigateToMap(){
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${viewModel.homePlacementsItemData.originCenterLat},${viewModel.homePlacementsItemData.originCenterLong}"+"(" + viewModel.homePlacementsItemData.originCenterName+ ")")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(this@PlacementDetailsActivity, "Unable to open map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessEditDialog(){

        val dialog = Dialog(this)
        val bindingDialog= DialogPlacementDetailsEditBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        if(!this.isFinishing)
            dialog.show()
        if(!this.isFinishing)
            Handler(Looper.myLooper()!!).postDelayed({
                dialog.dismiss()
                REFRESH_ON_BACK_PLACEMENT = true
                finish()
            }, 2000)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun showIntracityAdhocSuccessDialog(){
        // prepare dialog UI's and whatsapp share data
        val title = getString(R.string.title_dialog_success)
        val subTittle = getString(R.string.sub_title_dialog_success)
        val playStoreLink = getString(R.string.driver_app_link)
        val ticketId = "TKT123456"
        val reportingCentre = "https://maps.google.com/?q=Mumbai+MIDC"
        val reportingTime = "09:00 AM"
        val hindiVideoLink = "https://youtube.com/watch?v=hindi_video_id"
        val englishVideoLink = "https://youtube.com/watch?v=english_video_id"

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
                    // Handle dialog dismissal if needed
                    println("Dialog dismissed")
                }
            }
        )
    }
}

fun placementDetailsIntent(
    homePlacementsItemData: HomePlacementsItemData,
    context: Context
) = Intent(context, PlacementDetailsActivity::class.java).apply {
    putExtra(HOME_PLACEMENT_ITEM_DATA, homePlacementsItemData)

}
var REFRESH_ON_BACK_PLACEMENT = false
package com.delhivery.axle.ui.trucks

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.databinding.ActivityTruckBinding
import com.delhivery.axle.databinding.DialogAddTruckSuccessBinding
import com.delhivery.axle.databinding.DialogBottomTruckValueBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.regex.Pattern
import javax.inject.Inject


class TruckActivity : BaseActivity<ActivityTruckBinding, TruckViewModel>() {

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass()= TruckViewModel::class.java

    override fun layoutId() = R.layout.activity_truck

    override fun requireConnection() = true

    @Inject lateinit var autoCompleteUtils: AutoCompleteUtils
    @Inject lateinit var userPrefs: UserPrefs
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    var capacityArr = mutableListOf<String>()
    var sourcedAs : String = ""
    var truckItems = mutableListOf<TruckResponseArray>()

    val adapter :TruckSizeAdapter by lazy { TruckSizeAdapter() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("TruckActivity_SetupTime")
        activitySetupTrace?.start()
        /* get intent keys */
        viewModel.truckTypeIntent = intent.getStringExtra(TruckType) ?: ""
        viewModel.truckCapacityIntent = intent.getDoubleExtra(TruckCapacity,0.0) ?: 0.0
        viewModel.truckSizeIntent = intent.getStringExtra(TruckSize) ?: ""
        viewModel.minCapIntent = intent.getDoubleExtra(MinCap,0.0) ?: 0.0
        viewModel.maxCapIntent = intent.getDoubleExtra(MaxCap,0.0) ?: 0.0
        viewModel.sourcedAsIntent = intent.getStringExtra(Sourced)?: ""
        viewModel.fromLinks = intent.getBooleanExtra(FromLinks, false)
        viewModel.vehicleNumberIntent = intent.getStringExtra(VehicleNumber) ?: ""
        viewModel.addTruckSourceIntent = intent.getStringExtra(AddTruckSource) ?: ""

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_truck_details)

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
        }

        analyticsUtil.moEngageTrackEvent(
            EVENT_ADD_TRUCK_SUBMIT,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PAGE_NAME,
                PROPERTY_TOTAL_COUNT,
                PROPERTY_FASTAG_MAPPED
            ),
            mutableListOf(
                userPrefs.userId(),
                VALUE_TRUCKS_PAGE,
                viewModel.addTruckLiveDataRes.value?.inventoryId?:"",
                viewModel.addTruckLiveDataRes.value?.vehicleNumber?:"",
            ),
        )

        if(viewModel.fromLinks && viewModel.vehicleNumberIntent.isNotEmpty()){
            uiUtils.showProgress()
            viewModel.getInventory(userPrefs.userId() , viewModel.vehicleNumberIntent)
        }

        if(viewModel.truckTypeIntent.isNotEmpty()){
            when(viewModel.truckTypeIntent){
                BODY_TYPE_OPEN -> binding.btnRadioOpen.isChecked = true
                BODY_TYPE_CLOSED -> binding.btnRadioContainer.isChecked = true
                BODY_TYPE_TRAILER ->binding.btnRadioTrailer.isChecked = true
            }
        }

        if(viewModel.truckSizeIntent.isNotEmpty()){
            binding.textTruckSize.text = viewModel.truckSizeIntent
            sourcedAs = viewModel.sourcedAsIntent

            if(sourcedAs == SOURCED_AS_PMT){
                binding.labelPriceText.text = String.format(getString(R.string.label_price_mt))
                binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_pmt_price))
            }
            else if(sourcedAs == SOURCED_AS_FTL){
                binding.labelPriceText.text = String.format(getString(R.string.label_price_ftl))
                binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_ftl_price))
            }
            else{
                binding.labelPriceText.text = String.format(getString(R.string.label_price))
                binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_price))
            }

            var min = viewModel.minCapIntent
            val max = viewModel.maxCapIntent
            capacityArr.clear()

            while (min <= max) {
                capacityArr.add("$min MT")
                min += (1.0)
            }

        }

        if(viewModel.truckCapacityIntent != 0.0){
            binding.textTruckCapacity.text = "${viewModel.truckCapacityIntent} MT"
        }

        // Initialize button as disabled
        binding.btnAddTruck.isEnabled = false
        
        // Add text watcher for truck number
        binding.editTruckNumber.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                checkFieldsAndEnableButton()
            }
        })

        binding.bodyGroup.setOnCheckedChangeListener { radioGroup, i ->
            binding.textTruckSize.text=""
            binding.textTruckCapacity.text = ""
            sourcedAs = ""
            checkFieldsAndEnableButton()
        }

        binding.editTruckCapacity.setOnClickListener{
            if(binding.textTruckSize.text.isNotEmpty()) {
                showTruckCapacityDialog()
            }
            else{
                uiUtils.showToast(getString(R.string.msg_select_truck_size_first))
            }
        }

        binding.editTruckSize.setOnClickListener {
            if(truckItems.isNotEmpty()){
                val type = if(binding.btnRadioContainer.isChecked) "closed" else if(binding.btnRadioOpen.isChecked)  "open" else if(binding.btnRadioTrailer.isChecked) "trailer" else ""
                if(type != "") {
                    showTruckSizeDialog(type)
                }
                else{
                    uiUtils.showSnackbar("Select body type first")
                }
            }
            else{
                uiUtils.showSnackbar("No Truck Types Found")
            }
        }

        binding.editCurrentCity.setOnClickListener {
            startActivityForResult(searchCityIntent(this,"origin"), REQCODE_SELECT_CITY)

        }
        binding.editUnloadingDestinations.setOnClickListener {
            startActivityForResult(searchCityIntent(this,"destination"), REQCODE_SELECT_CITY)
        }

        binding.btnAddTruck.setOnClickListener{
           validateFieldsAndAddTruck()
        }

        viewModel.fetchTruckType()

        /** Observe live data*/
        viewModel.inventoryLiveData.observe(this, Observer {
            uiUtils.hideProgress()
            if(it != null){
                binding.editTruckNumber.setText(it.vehicleNumber)
                when(it.truckType){
                    BODY_TYPE_OPEN -> binding.btnRadioOpen.isChecked = true
                    BODY_TYPE_CLOSED -> binding.btnRadioContainer.isChecked = true
                    BODY_TYPE_TRAILER -> binding.btnRadioTrailer.isChecked = true
                }

                when(it.ownership){
                    OWNERSHIP_OWN -> binding.btnRadioOwnTruck.isChecked = true
                    OWNERSHIP_MARKET -> binding.btnRadioMarketTruck.isChecked = true
                }

                capacityArr.clear()
                capacityArr.add("${it.capacity} MT")


                binding.textTruckSize.text = it.truckSize
                binding.textTruckCapacity.text = it.truckCapacity()

                if(it.currentCityName!= null && it.currentCityCode!=null){
                    val cityModel = CityModel(it.currentCityName!! , it.currentCityCode)
                    viewModel.truckCity = cityModel
                    binding.textCurrentCity.text = viewModel.truckCity!!.cityName()
                }

                if(it.unloadingDestination != null &&  it.unloadingDestinationCode != null){
                    val cityModel = CityModel(it.unloadingDestination!! , it.unloadingDestinationCode)
                    viewModel.truckDestination = cityModel
                    binding.textUnloadingDestination.text = viewModel.truckDestination!!.cityName()
                }

                sourcedAs = it.sourcedAs ?:""

                if(it.sourcedAs != null && it.sourcedAs == SOURCED_AS_PMT){
                    binding.labelPriceText.text = String.format(getString(R.string.label_price_mt))
                    binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_pmt_price))
                }
                else if(it.sourcedAs != null && it.sourcedAs == SOURCED_AS_FTL){
                    binding.labelPriceText.text = String.format(getString(R.string.label_price_ftl))
                    binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_ftl_price))
                }
                else{
                    binding.labelPriceText.text = String.format(getString(R.string.label_price))
                    binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_price))
                }

            }
            else{
                binding.editTruckNumber.setText(viewModel.truckNumber)
                uiUtils.showSnackbar(getString(R.string.msg_no_inventory_found))
            }
        })

        viewModel.truckGetLiveData.observe(this, Observer {
            if(it!=null){
                truckItems.addAll(it)
            }
        })

        viewModel.addTruckLiveData.observe(this, Observer{
            uiUtils.hideProgress()
            if(it!=null && it == true){
                showTruckAddedDialog()
                analyticsUtil.moEngageTrackEvent(
                    EVENT_ADD_TRUCK_SUBMIT,
                    mutableListOf(
                        PROPERTY_USER_ID,
                        PROPERTY_INVENTORY_UUID,
                        PROPERTY_VEHICLE_NUMBER,
                        PROPERTY_PAGE_NAME,
                        ),
                    mutableListOf(
                        userPrefs.userId(),
                        viewModel.addTruckLiveDataRes.value?.inventoryId?:"",
                        viewModel.addTruckLiveDataRes.value?.vehicleNumber?:"",
                        VALUE_TRUCKS_PAGE
                    ),
                )
            }
            else if(it!=null && it== false){
                dialogUtils.showErrorDialog(
                    getString(R.string.error_city_not_mapped),
                    3L
                )
            }
        })
        viewModel.noCityCodeError.observe(this, Observer {
            if(it){
                uiUtils.hideProgress()
                dialogUtils.showErrorDialog(
                    getString(R.string.error_city_code_missing),
                    3L
                )
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
    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
            "[a-zA-Z]{2}((([0-9]{1,2}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}\$|^[a-zA-Z]{3}[0-9]{4}"
        )
        return pattern.matcher(number).matches()
    }

    private fun showTruckAddedDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogAddTruckSuccessBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        dialog.show()
        Handler(Looper.myLooper()!!).postDelayed({
            dialog.dismiss()
            setResult(REQCODE_ADD_TRUCK, Intent().apply {
                putExtra(getString(R.string.intent_extra_added), getString(R.string.intent_extra_truck_added))
            })
            finish()
        }, 2000)

        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    /**
     * Checks if all required fields are filled and enables/disables the Add Truck button accordingly.
     * Price field is optional and not considered for button enablement.
     */
    private fun checkFieldsAndEnableButton() {
        binding.btnAddTruck.isEnabled = areAllRequiredFieldsFilled()
    }
    
    /**
     * Validates that all required fields are filled.
     * @return true if all required fields have valid values, false otherwise
     */
    private fun areAllRequiredFieldsFilled(): Boolean {
        return isTruckNumberValid() &&
               isTruckSizeValid() &&
               isTruckCapacityValid() &&
               isCurrentCityValid() &&
               isDestinationValid() &&
               isBodyTypeSelected()
    }
    
    private fun isTruckNumberValid(): Boolean = 
        binding.editTruckNumber.text?.toString()?.isNotEmpty() == true
    
    private fun isTruckSizeValid(): Boolean = 
        binding.textTruckSize.text.toString().isNotEmpty()
    
    private fun isTruckCapacityValid(): Boolean = 
        binding.textTruckCapacity.text.toString().isNotEmpty()
    
    private fun isCurrentCityValid(): Boolean = 
        viewModel.truckCity != null
    
    private fun isDestinationValid(): Boolean = 
        viewModel.truckDestination != null
    
    private fun isBodyTypeSelected(): Boolean = 
        binding.btnRadioContainer.isChecked || 
        binding.btnRadioOpen.isChecked || 
        binding.btnRadioTrailer.isChecked

    private fun getSelectedBodyType(): String = when {
        binding.btnRadioContainer.isChecked -> BODY_TYPE_CLOSED
        binding.btnRadioOpen.isChecked -> BODY_TYPE_OPEN
        binding.btnRadioTrailer.isChecked -> BODY_TYPE_TRAILER
        else -> ""
    }


    private fun validateFieldsAndAddTruck(){
        viewModel.truckOwnership = if(binding.btnRadioMarketTruck.isChecked)
            OWNERSHIP_MARKET else OWNERSHIP_OWN

        viewModel.truckCapacity =  if(binding.textTruckCapacity.text.isNotEmpty())
            binding.textTruckCapacity.text.toString().split("\\s+".toRegex())[0].toDouble() else 0.0

        viewModel.truckSize = if(binding.textTruckSize.text.isNotEmpty()) binding.textTruckSize.text.toString() else ""

        viewModel.truckNumber = binding.editTruckNumber.text.toString().trim()

        viewModel.truckType = getSelectedBodyType()

        viewModel.truckPrice = binding.editPriceAddTruck.text.toString().toIntOrNull()?.toDouble() ?: 0.0

        var flag = true

        if(viewModel.truckType.isEmpty()){
            flag = false
            uiUtils.showSnackbar(getString(R.string.msg_select_body_type))
        }

        if(viewModel.truckCapacity!= 0.0 ){
            binding.capacityError.visibility = View.GONE
        }
        else{
            binding.capacityError.text = String.format(getString(R.string.msg_empty_field))
            binding.capacityError.visibility = View.VISIBLE
            flag = false
        }
        if(viewModel.truckNumber.isNotEmpty() && validateTruckNumber(viewModel.truckNumber)){
            binding.numberError.visibility = View.GONE
        }
        else if(viewModel.truckNumber.isNotEmpty() && !validateTruckNumber(viewModel.truckNumber)){
            binding.numberError.text = String.format(getString(R.string.incorrect_vehicle_number))
            binding.numberError.visibility = View.VISIBLE
            flag= false
        }
        else {
            binding.numberError.text = String.format(getString(R.string.msg_empty_field))
            binding.numberError.visibility = View.VISIBLE
            flag= false
        }
        if(viewModel.truckSize.isNotEmpty()) {
            binding.sizeError.visibility = View.GONE
       }
        else {
            binding.sizeError.text = String.format(getString(R.string.msg_empty_field))
            binding.sizeError.visibility = View.VISIBLE
            flag= false
        }
        if(viewModel.truckCity!=null ){
            binding.originError.visibility = View.GONE
        } else{
            binding.originError.text = String.format(getString(R.string.msg_empty_field))
            binding.originError.visibility = View.VISIBLE
            flag= false
        }
        if(viewModel.truckDestination!= null){
            binding.destinationError.visibility= View.GONE
        }
        else{
            binding.destinationError.text = String.format(getString(R.string.msg_empty_field))
            binding.destinationError.visibility = View.VISIBLE
            flag= false
        }

        if(flag) {
            uiUtils.showProgress(getString(R.string.progress_adding_truck))
            viewModel.addNewTruck(sourcedAs.uppercase())
        }

    }


    private fun showTruckSizeDialog(type: String) {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = getString(R.string.label_select_truck_size)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val truckSizeList = mutableListOf<TruckResponseArray>()

        for(truck in (truckItems.sortedByDescending { it.truckUuid }.reversed()).sortedBy { it.defaultMG }){
            if(truck.truckType == type){
                truckSizeList.add(truck)
            }
        }
        adapter.setItems(truckSizeList)
        bindingDialog.truckList.adapter = this@TruckActivity.adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position).truckUuid
            sourcedAs = adapter.getItem(position).sourcedAs?: ""
            var min = adapter.getItem(position).minCapacity
            val max = adapter.getItem(position).maxCapacity
            if(sourcedAs == "PMT"){
                binding.labelPriceText.text = String.format(getString(R.string.label_price_mt))
                binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_pmt_price))
            }
            else if(sourcedAs == "FTL"){
                binding.labelPriceText.text = String.format(getString(R.string.label_price_ftl))
                binding.editPriceAddTruck.hint = String.format(getString(R.string.hint_ftl_price))
            }
            viewModel.truckCapacity = 0.0
            binding.textTruckCapacity.text = ""
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            checkFieldsAndEnableButton()
            dialog.dismiss()
        }

        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = getString(R.string.label_select_truck_capacity)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, capacityArr);
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckCapacity.text = adapter.getItem(position)
            checkFieldsAndEnableButton()
            dialog.dismiss()
        }
        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQCODE_SELECT_CITY ->{
                if(data != null) {
                    val type = data.getStringExtra(CityType)
                    val city = data.getSerializable("City",CityModel::class.java)!!
                    if(type =="origin") {
                        viewModel.truckCity = city
                        binding.textCurrentCity.text = city.cityName().trim()
                    }
                    else if(type == "destination"){
                        viewModel.truckDestination = city
                        binding.textUnloadingDestination.text = city.cityName().trim()
                    }
                    checkFieldsAndEnableButton()
                }
            }
        }
    }


}

/* intent Keys */
private const val TruckType = "truck_type"
private const val TruckSize = "truck_size"
private const val TruckCapacity = "truck_capacity"
private const val CityType = "city_type"
private const val MinCap = "min_cap"
private const val MaxCap = "max_cap"
private const val Sourced = "sources_as"
private const val FromLinks = "notification_deeplink"
private const val VehicleNumber = "vehicle_number"
private const val AddTruckSource = "source"


private const val BODY_TYPE_CLOSED = "closed"
private const val BODY_TYPE_OPEN = "open"
private const val BODY_TYPE_TRAILER = "trailer"
private const val OWNERSHIP_MARKET = "market_truck"
private const val OWNERSHIP_OWN = "owns_truck"

// Sourced types
private const val SOURCED_AS_PMT = "PMT"
private const val SOURCED_AS_FTL = "FTL"

// Request codes
private const val REQCODE_SELECT_CITY = 100



/**
 * Truck intent
 */
fun truckIntent(
    context: Context,
    truckType: String = "",
    truckSize: String= "",
    truckCapacity: Double= 0.0,
    minCap :Double = 0.0,
    maxCap: Double =0.0,
    sourcesAS: String= "",
    fromLinks:Boolean = false,
    vehicleNumber: String= "",
    source:String=""
) = Intent(context, TruckActivity::class.java).apply {
    putExtra(TruckType, truckType)
    putExtra(TruckSize, truckSize)
    putExtra(TruckCapacity, truckCapacity)
    putExtra(MinCap, minCap)
    putExtra(MaxCap, maxCap)
    putExtra(Sourced, sourcesAS)
    putExtra(FromLinks, fromLinks)
    putExtra(VehicleNumber , vehicleNumber)
    putExtra(AddTruckSource , source)

}
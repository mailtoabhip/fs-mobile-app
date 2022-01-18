package com.delhivery.axle.ui.trucks

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.delhivery.axle.databinding.DialogBottomTruckUnloadingDetailsBinding
import com.delhivery.axle.databinding.DialogBottomTruckValueBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.TruckSpinnerAdapter
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
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

    var capacityArr = mutableListOf<String>()
    var sourcedAs : String = ""
    var truckItems = mutableListOf<TruckResponseArray>()

    val adapter :TruckSizeAdapter by lazy { TruckSizeAdapter() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* get intent keys */
        viewModel.truckTypeIntent = intent.getStringExtra(TruckType) ?: ""
        viewModel.truckCapacityIntent = intent.getDoubleExtra(TruckCapacity,0.0) ?: 0.0
        viewModel.truckSizeIntent = intent.getStringExtra(TruckSize) ?: ""
        viewModel.minCapIntent = intent.getDoubleExtra(MinCap,0.0) ?: 0.0
        viewModel.maxCapIntent = intent.getDoubleExtra(MaxCap,0.0) ?: 0.0
        viewModel.sourcedAsIntent = intent.getStringExtra(Sourced)?: ""


    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Enter Truck Details"

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
        }

        if(viewModel.truckTypeIntent != ""){
            when(viewModel.truckTypeIntent){
                "open" -> binding.btnRadioOpen.isChecked = true
                "closed" -> binding.btnRadioContainer.isChecked = true
                "trailer" ->binding.btnRadioTrailer.isChecked = true
            }
        }

        if(viewModel.truckSizeIntent != ""){
            binding.textTruckSize.text = viewModel.truckSizeIntent
            sourcedAs = viewModel.sourcedAsIntent
            val min = viewModel.minCapIntent.toInt()
            val max = viewModel.maxCapIntent.toInt()
            capacityArr.clear()

            for ( i in min..max){
                capacityArr.add("$i MT")
            }

        }

        if(viewModel.truckCapacityIntent != 0.0){
            binding.textTruckCapacity.text = "${(viewModel.truckCapacityIntent).toInt()} MT"
        }

        binding.editTruckCapacity.setOnClickListener{
            if(binding.textTruckSize.text != "") {
                showTruckCapacityDialog()
            }
            else{
                uiUtils.showToast("Select Truck Size First")
            }
        }

        binding.editTruckSize.setOnClickListener {
            showTruckSizeDialog()
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

        //Live Data Observers

        viewModel.truckGetLiveData.observe(this, Observer {
            if(it!=null){
                truckItems.addAll(it)
                adapter.setItems(it)
            }
        })

        viewModel.addTruckLiveData.observe(this, Observer{
            uiUtils.hideProgress()
            if(it!=null && it == true){

            }
        })

    }

    private fun validateFieldsAndAddTruck(){
        viewModel.truckOwnership = if(binding.btnRadioMarketTruck.isChecked)
            "market_truck" else "owns_truck"

        viewModel.truckCapacity =  if(binding.textTruckCapacity.text.isNotEmpty())
            binding.textTruckCapacity.text.toString().split("\\s+".toRegex())[0].toInt().toDouble() else 0.0

        viewModel.truckSize = if(binding.textTruckSize.text.isNotEmpty()) binding.textTruckSize.text.toString() else ""

        viewModel.truckNumber = if(binding.editTruckNumber.text !=null && binding.editTruckNumber.text.toString() != "" )
            binding.editTruckNumber.text.toString() else ""

        viewModel.truckType = if(binding.btnRadioContainer.isChecked) "closed" else if(binding.btnRadioOpen.isChecked)  "open" else "trailer"

        viewModel.truckPrice = if(binding.editPriceAddTruck.text !=null && binding.editPriceAddTruck.text.toString() != "" )
            binding.editPriceAddTruck.text.toString().toInt().toDouble() else 0.0

        var flag = true

        if(viewModel.truckCapacity!= 0.0 ){
            binding.capacityError.visibility = View.GONE
        }
        else{
            binding.capacityError.text = String.format(getString(R.string.msg_empty_field))
            binding.capacityError.visibility = View.VISIBLE
            flag = false
        }
        if(viewModel.truckNumber.isNotEmpty()){
            binding.numberError.visibility = View.GONE
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
        if((sourcedAs.toUpperCase() != "FTL" && sourcedAs.toUpperCase() != "PMT") ||
            ((sourcedAs.toUpperCase() == "FTL" || sourcedAs.toUpperCase() == "PMT") && viewModel.truckPrice != 0.0)){
            binding.priceError.visibility =View.GONE
        }
        else{
            binding.priceError.text = String.format(getString(R.string.msg_empty_field))
            binding.priceError.visibility = View.VISIBLE
            flag = false
        }

        if(flag) {
            analyticsUtil.trackEvent(
                EVENT_ADD_TRUCK,
                mutableListOf(PROPERTY_USER_ID),
                mutableListOf(userPrefs.userId())
            )
            uiUtils.showProgress("Adding truck")
            viewModel.addNewTruck(sourcedAs.toUpperCase())
        }

    }


    private fun showTruckSizeDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = getString(R.string.label_select_truck_size)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.truckList.adapter = this@TruckActivity.adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position).truckUuid
            sourcedAs = adapter.getItem(position).sourcedAs?: ""
            val min = adapter.getItem(position).minCapacity
            val max = adapter.getItem(position).maxCapacity
            viewModel.truckCapacity = 0.0
            binding.textTruckCapacity.text = ""
            capacityArr.clear()
            if(min !=null &&  max!=null){
                for ( i in min.toInt()..max.toInt()){
                    capacityArr.add("$i MT")
                }
            }
            dialog.dismiss()
        }

        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
                    val city = data.getSerializableExtra("City") as CityModel
                    if(type =="origin") {
                        viewModel.truckCity = city
                        binding.textCurrentCity.text = city.cityName().trim()
                    }
                    else if(type == "destination"){
                        viewModel.truckDestination = city
                        binding.textUnloadingDestination.text = city.cityName().trim()
                    }
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
    sourcesAS: String= ""
) = Intent(context, TruckActivity::class.java).apply {
    putExtra(TruckType, truckType)
    putExtra(TruckSize, truckSize)
    putExtra(TruckCapacity, truckCapacity)
    putExtra(MinCap, minCap)
    putExtra(MaxCap, maxCap)
    putExtra(Sourced, sourcesAS)

}
package com.delhivery.axle.ui.trucks

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trips.FuelUserSpinnerOptions
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.databinding.DialogBottomEditTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class EditTruckDialog @Inject constructor(
    context: Context,
    private val data: HomeTrucksRequestItemData,
    private val dialogInterface: EditTruckInterface,
    private val userPrefs: UserPrefs,
    private val analyticsUtil: AnalyticsUtil,
    private val uiUtils: UiUtils,
    private val position:Int
) : AlertDialog(context){
    private lateinit var binding: DialogBottomEditTruckBinding

    var truckCity : CityModel? =null
    var truckDestination: CityModel? = null

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra(CityType)
            val data = intent.getSerializableExtra("City") as CityModel
            if (city != null && data !=null) {
                if(city =="origin"){
                    truckCity = data
                    binding.textCurrentCityEditDialog.text = data.cityName()
                }
                else if(city =="destination"){
                    truckDestination = data
                    binding.textUnloadingDestinationEditDialog.text = data.cityName()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        binding = DialogBottomEditTruckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

        //set Broadcast receiver
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, IntentFilter("get_selected_city"))

        //Set Previous values
        if(data.currentCityName!= null && data.currentCityCode!=null){
            truckCity!!.city =  data.currentCityName!!
            truckCity!!.orion_db_city_code = data.currentCityCode
        }

        if(data.unloadingDestination != null &&  data.unloadingDestinationCode != null){
            truckDestination!!.city = data.unloadingDestination!!
            truckDestination!!.orion_db_city_code = data.unloadingDestinationCode
        }

        if(data.ownership.isNotNullOrEmpty()){
            when(data.ownership){
                "owns_truck" -> binding.btnRadioOwnTruckEditDialog.isChecked = true
                "market_truck" -> binding.btnRadioMarketTruckEditDialog.isChecked = true
            }
        }


        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.editCurrentCityEditDialog.setOnClickListener{
            context.startActivity(searchCityIntent(context, "origin",true))
        }
        binding.editUnloadingDestinationsEditDialog.setOnClickListener{
            context.startActivity(searchCityIntent(context, "destination",true))
        }

        binding.btnSaveEditChanges.setOnClickListener{
            validateFields()
        }

    }

    private fun validateFields() {
        val truckPrice= if(binding.editPrice.text != null && binding.editPrice.text.toString() != "")
            binding.editPrice.text.toString().toInt().toDouble()
        else 0.0

        var flag = true
        if ( truckPrice != 0.0){
            binding.priceErrorEdit.visibility = View.GONE
        }
        else{
            binding.priceErrorEdit.text = String.format(context.getString(R.string.msg_empty_field))
            binding.priceErrorEdit.visibility = View.VISIBLE
            flag = false
        }
        if(truckCity != null){
            binding.originErrorEdit.visibility = View.GONE
        }
        else{
            binding.originErrorEdit.text = String.format(context.getString(R.string.msg_empty_field))
            binding.originErrorEdit.visibility = View.VISIBLE
            flag = false
        }
        if(truckDestination != null){
            binding.destinationErrorEdit.visibility = View.GONE
        }
        else{
            binding.destinationErrorEdit.text = String.format(context.getString(R.string.msg_empty_field))
            binding.destinationErrorEdit.visibility = View.VISIBLE
            flag = false
        }

        if (flag){
            analyticsUtil.trackEvent(
                EVENT_EDIT_TRUCK,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID),
                mutableListOf(userPrefs.userId(), data.inventoryId)
            )
            uiUtils.showProgress()
            dialogInterface.editTruck(data.inventoryId, truckCity!!, truckDestination!! ,"FTL", truckPrice, position)
        }
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
        super.setOnCancelListener(listener)
    }
}

interface EditTruckInterface{
    fun editTruck(
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int
    )
}


private const val CityType = "city_type"
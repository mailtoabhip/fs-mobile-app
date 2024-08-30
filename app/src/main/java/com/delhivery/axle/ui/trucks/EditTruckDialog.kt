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
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.databinding.DialogBottomEditTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getSerializable
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
    var sourcedAs: String = ""

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra(CityType)
            val data = intent.getSerializable("City",CityModel::class.java)
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
        ContextCompat.registerReceiver(context,mMessageReceiver, IntentFilter("get_selected_city"),ContextCompat.RECEIVER_NOT_EXPORTED)

        //Set Previous values
        binding.request = data

        if(data.currentCityName!= null && data.currentCityCode!=null){
            val cityModel = CityModel(data.currentCityName!! , data.currentCityCode)
            truckCity = cityModel
            binding.textCurrentCityEditDialog.text = truckCity!!.cityName()
        }

        if(data.unloadingDestination != null &&  data.unloadingDestinationCode != null){
            val cityModel = CityModel(data.unloadingDestination!! , data.unloadingDestinationCode)
            truckDestination = cityModel
            binding.textUnloadingDestinationEditDialog.text = truckDestination!!.cityName()
        }

        if(data.unloadingDestinationAmount != null && data.unloadingDestinationAmount != 0.0){
            binding.editPrice.setText( data.unloadingDestinationAmount!!.toInt().toString())
            sourcedAs = "FTL"
        }
        else if(data.unloadingDestinationRate !=null && data.unloadingDestinationRate != 0.0){
            binding.editPrice.setText( data.unloadingDestinationRate!!.toInt().toString())
            sourcedAs = "PMT"
        }

        if(data.sourcedAs != null) {
            sourcedAs = data.sourcedAs!!
        }

        if(sourcedAs == "PMT"){
            binding.editPrice.hint = String.format(context.getString(R.string.hint_pmt_price))
            binding.labelTextPriceEditDialog.text = String.format(context.getString(R.string.label_price_mt))
        }
        else if(sourcedAs == "FTL"){
            binding.editPrice.hint = String.format(context.getString(R.string.hint_ftl_price))
            binding.labelTextPriceEditDialog.text = String.format(context.getString(R.string.label_price_ftl))
        }
        else{
            binding.editPrice.hint = String.format(context.getString(R.string.hint_price))
            binding.labelTextPriceEditDialog.text = String.format(context.getString(R.string.label_price))
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

        val truckOwnership = if(binding.btnRadioMarketTruckEditDialog.isChecked)
            "market_truck" else "owns_truck"

        var flag = true

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
            dialogInterface.editTruck(data, truckCity!!, truckDestination!! ,sourcedAs, truckPrice, truckOwnership, position)
            dismiss()
        }
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
        super.setOnCancelListener(listener)
    }
}

interface EditTruckInterface{
    fun editTruck(
        data: HomeTrucksRequestItemData,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        ownership:String,
        position: Int
    )
}


private const val CityType = "city_type"
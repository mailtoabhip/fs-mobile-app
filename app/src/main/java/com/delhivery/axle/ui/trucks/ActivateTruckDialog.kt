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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.databinding.DialogBottomActivateTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.spinner_item.view.*
import javax.inject.Inject

class ActivateTruckDialog @Inject constructor(
    context: Context,
    private val data: HomeTrucksRequestItemData,
    private val dialogInterface: ActivateTruckInterface,
    private val userPrefs: UserPrefs,
    private val analyticsUtil: AnalyticsUtil,
    private val uiUtils: UiUtils,
    private val position:Int,
    private val fromDeepLink:Boolean=false,
    private val fromNotification:Boolean=false

) : AlertDialog(context){
    private lateinit var binding: DialogBottomActivateTruckBinding

    var truckCity : CityModel? =null
    var truckDestination: CityModel? = null
    var sourcedAs: String = ""

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra(CityType)
            val data = intent.getSerializableExtra("City") as CityModel
            if (city != null && data !=null) {
                if(city =="origin"){
                    truckCity = data
                    binding.textCurrentCityActivate.text = data.cityName()
                }
                else if(city =="destination"){
                    truckDestination = data
                    binding.textUnloadingDestinationActivate.text = data.cityName()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
        binding = DialogBottomActivateTruckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set Broadcast receiver
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, IntentFilter("get_selected_city"))

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

        if(fromDeepLink||fromNotification){
            binding.textCurrentCityActivate.text = data.destinationCity()
            binding.textUnloadingDestinationActivate.text = data.originCity()
            val originCityModel = CityModel(data.destinationCity()!!,data.currentCityCode,null,null)
            val unloadingCityModel = CityModel(data.originCity()!!,data.unloadingDestinationCode,null,null)
            truckCity =  originCityModel
            truckDestination = unloadingCityModel
        }
        //Set Previous Values
        if(data.unloadingDestinationAmount != null){
            sourcedAs = "FTL"
        }
        else if(data.unloadingDestinationRate !=null){
            sourcedAs = "PMT"
        }

        if(data.sourcedAs != null) {
            sourcedAs = data.sourcedAs!!
        }

        if(sourcedAs == "PMT"){
            binding.editPriceActivateTruck.hint = String.format(context.getString(R.string.hint_pmt_price))
            binding.labelTextPriceActivateDialog.text = String.format(context.getString(R.string.label_price_mt))
        }
        else if(sourcedAs == "FTL"){
            binding.editPriceActivateTruck.hint = String.format(context.getString(R.string.hint_ftl_price))
            binding.labelTextPriceActivateDialog.text = String.format(context.getString(R.string.label_price_ftl))
        }
        else{
            binding.editPriceActivateTruck.hint = String.format(context.getString(R.string.hint_price))
            binding.labelTextPriceActivateDialog.text = String.format(context.getString(R.string.label_price))
        }

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.editCurrentCityLayoutActivate.setOnClickListener{
            context.startActivity(searchCityIntent(context, "origin",true))
        }
        binding.editUnloadingDestinationsLayoutActivate.setOnClickListener{
            context.startActivity(searchCityIntent(context, "destination",true))
        }

        binding.btnActivateTruckDialog.setOnClickListener{
            validateFields()
        }



    }

    private fun validateFields() {
         val truckPrice= if(binding.editPriceActivateTruck.text != null && binding.editPriceActivateTruck.text.toString() != "")
             binding.editPriceActivateTruck.text.toString().toInt().toDouble()
        else 0.0

        var flag = true

        if(truckCity != null){
            binding.originErrorActivate.visibility = View.GONE
        }
        else{
            binding.originErrorActivate.text = String.format(context.getString(R.string.msg_empty_field))
            binding.originErrorActivate.visibility = View.VISIBLE
            flag = false
        }
        if(truckDestination != null){
            binding.destinationErrorActivate.visibility = View.GONE
        }
        else{
            binding.destinationErrorActivate.text = String.format(context.getString(R.string.msg_empty_field))
            binding.destinationErrorActivate.visibility = View.VISIBLE
            flag = false
        }

        if (flag){
            when {
                fromDeepLink -> {
                    analyticsUtil.trackEvent(
                        EVENT_ACTIVATE_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID, PROPERTY_SOURCE),
                        mutableListOf(userPrefs.userId(), data.inventoryId, VALUE_DEEP_LINKING)
                    )
                }
                fromNotification -> {
                    analyticsUtil.trackEvent(
                        EVENT_ACTIVATE_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID, PROPERTY_SOURCE),
                        mutableListOf(userPrefs.userId(), data.inventoryId, VALUE_NOTIFICATION)
                    )
                }
                else -> {
                    analyticsUtil.trackEvent(
                        EVENT_ACTIVATE_TRUCK,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_INVENTORY_ID),
                        mutableListOf(userPrefs.userId(), data.inventoryId)
                    )
                }
            }

            uiUtils.showProgress()
            dialogInterface.activateTruck(data, data.inventoryId, truckCity!!, truckDestination!! ,sourcedAs, truckPrice, position)
            dismiss()
        }
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
        super.setOnCancelListener(listener)
    }
}

interface ActivateTruckInterface{

    fun activateTruck(
        data: HomeTrucksRequestItemData,
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int
    )
}


private const val CityType = "city_type"
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
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class ActivateTruckDialog @Inject constructor(
    context: Context,
    private val data: HomeTrucksRequestItemData,
    private val dialogInterface: ActivateTruckInterface,
    private val userPrefs: UserPrefs,
    private val analyticsUtil: AnalyticsUtil,
    private val uiUtils: UiUtils,
    private val position:Int
) : AlertDialog(context){
    private lateinit var binding: DialogBottomActivateTruckBinding

    var truckCity : CityModel? =null
    var truckDestination: CityModel? = null

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
             binding.editPriceActivateTruck.text.toString().toInt()
        else 0

        var flag = true
        if ( truckPrice != 0){
            binding.priceErrorActivate.visibility = View.GONE
        }
        else{
            binding.priceErrorActivate.text = String.format(context.getString(R.string.msg_empty_field))
            binding.priceErrorActivate.visibility = View.VISIBLE
            flag = false
        }
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
            uiUtils.showProgress()
            //dialogInterface.activateTruck(data.inventoryId, position)
        }
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
        super.setOnCancelListener(listener)
    }
}

interface ActivateTruckInterface{

    fun activateTruck(
        inventoryId: String,
        currentCity: CityModel,
        destinationCity: CityModel,
        sourcedAs: String,
        price: Double,
        position: Int
    )
}


private const val CityType = "city_type"
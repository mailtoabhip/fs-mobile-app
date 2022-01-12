package com.delhivery.axle.ui.trucks

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.databinding.DialogBottomActivateTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import javax.inject.Inject

class ActivateTruckDialog @Inject constructor(
    context: Context,
    position:Int
) : AlertDialog(context){
    private lateinit var binding: DialogBottomActivateTruckBinding

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra(CityType)
            val data = intent.getSerializableExtra("City") as CityModel
            if (city != null && data !=null) {
                if(city =="origin"){
                    binding.textCurrentCityActivate.text = data.cityName()
                }
                else if(city =="destination"){
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



    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
        super.setOnCancelListener(listener)
    }
}

private const val CityType = "city_type"
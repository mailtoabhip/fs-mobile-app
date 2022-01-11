package com.delhivery.axle.ui.trucks

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.delhivery.axle.databinding.DialogBottomEditTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import javax.inject.Inject

class EditTruckDialog @Inject constructor(
    context: Context,
    position:Int

) : AlertDialog(context){
    private lateinit var binding: DialogBottomEditTruckBinding

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra(CityType)
            val data = intent.getSerializableExtra("City") as CityModel
            if (city != null && data !=null) {
                if(city =="origin"){
                    binding.textCurrentCityEditDialog.text = data.cityName()
                }
                else if(city =="destination"){
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

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.editCurrentCityEditDialog.setOnClickListener{
            context.startActivity(searchCityIntent(context, "origin",true))
        }
        binding.editUnloadingDestinationsEditDialog.setOnClickListener{
            context.startActivity(searchCityIntent(context, "destination",true))
        }

    }
}


private const val CityType = "city_type"
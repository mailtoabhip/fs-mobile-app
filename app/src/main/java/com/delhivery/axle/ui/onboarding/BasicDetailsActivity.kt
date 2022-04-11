package com.delhivery.axle.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBasicDetailsBinding
import com.delhivery.axle.databinding.ActivityOnboardingBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.ui.base.BaseActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.StateModelList
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.searchcitystate.CityType
import com.delhivery.axle.ui.searchcitystate.SelectedData
import com.delhivery.axle.ui.searchcitystate.searchCityIntent
import com.delhivery.axle.ui.searchcitystate.searchOriginCityIntent
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import java.lang.StringBuilder


class BasicDetailsActivity: BaseActivity<ActivityBasicDetailsBinding, BasicDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#181818")
    }
    var selectedOrigin:String? = null
    override fun getViewModelClass() = BasicDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_basic_details

    override fun requireConnection() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        binding.checkBoxOpenBody.setOnClickListener {

            if( binding.imgOpenTruck.isSelected){
                binding.imgOpenTruck.isSelected = false
                binding.checkBoxOpenBody.isSelected =false
                binding.txtOpenBody.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgOpenTruck.isSelected = true
                binding.checkBoxOpenBody.isSelected =true
                binding.txtOpenBody.setTextColor(this.resources.getColor(R.color.dark_blue))
            }
            enableSubmit()
        }
        binding.checkBoxContainer.setOnClickListener {
            if( binding.imgContainer.isSelected){
                binding.imgContainer.isSelected = false
                binding.checkBoxContainer.isSelected =false
                binding.txtContainer.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgContainer.isSelected = true
                binding.checkBoxContainer.isSelected =true
                binding.txtContainer.setTextColor(this.resources.getColor(R.color.dark_blue))
            }
            enableSubmit()
        }
        binding.checkBoxTrailer.setOnClickListener {
            if( binding.imgTrailer.isSelected){
                binding.imgTrailer.isSelected = false
                binding.checkBoxTrailer.isSelected =false
                binding.txtTrailer.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgTrailer.isSelected = true
                binding.checkBoxTrailer.isSelected =true
                binding.txtTrailer.setTextColor(this.resources.getColor(R.color.dark_blue))
            }
            enableSubmit()
        }
        binding.editOrigin.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType,"origin")
            if(selectedOrigin!=null){
            bundle.putString(SelectedData,selectedOrigin)
            }
            navigationUtils.navigateForActivityResult(
                intent = searchOriginCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_SELECT_CITY, extras = bundle
            )
        }
        binding.editDestination.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType,"destination")
            navigationUtils.navigateForActivityResult(
                intent = searchCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_DESTINATION_SELECT_CITY, extras = bundle
            )
        }
     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQCODE_SELECT_CITY -> {
                    if(data != null) {
                        val type = data.getStringExtra(CityType)
                        val city = data.getSerializableExtra("City") as CityModel
                        if(type =="origin") {
                            selectedOrigin = city.cityName()
                            binding.editOrigin.setText(city.cityName().trim())
                            enableSubmit()
                        }

                    }
                }
                REQCODE_DESTINATION_SELECT_CITY -> {
                    if(data != null) {
                        val type = data.getStringExtra(CityType)
                        val cities: ArrayList<CityModel> =
                            data.getSerializableExtra("City") as ArrayList<CityModel>
                        val citiesNames = ArrayList<String>()
                        if(type =="destination") {
                            for(item in cities){
                                citiesNames.add(item.cityName())
                            }
                            binding.editDestination.setText(citiesNames.joinToString(separator = ", "))
                            enableSubmit()
                        }

                    }
                }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun enableSubmit(){
        binding.btnSubmitDetails.isEnabled = !binding.editDestination.text.isNullOrEmpty() && !binding.editOrigin.text.isNullOrEmpty()&&(binding.checkBoxContainer.isSelected||binding.checkBoxOpenBody.isSelected||binding.checkBoxTrailer.isSelected)
    }
}


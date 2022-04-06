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
import com.delhivery.axle.ui.searchcitystate.searchCityIntent
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY


class BasicDetailsActivity: BaseActivity<ActivityBasicDetailsBinding, BasicDetailsViewModel>(),
    BaseDataRVAdapter.ItemClickListener<CityModel> {
    init {
        StatusBarColor = Color.parseColor("#181818")
    }
    private var selectedCityStates = mutableSetOf<CityModel>()
   // private var states = StateModelList.toMutableList()
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
                binding.txtOpenBody.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgOpenTruck.isSelected = true
                binding.txtOpenBody.setTextColor(this.resources.getColor(R.color.dark_blue))
            }

        }
        binding.checkBoxContainer.setOnClickListener {
            if( binding.imgContainer.isSelected){
                binding.imgContainer.isSelected = false
                binding.txtContainer.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgContainer.isSelected = true
                binding.txtContainer.setTextColor(this.resources.getColor(R.color.dark_blue))
            }

        }
        binding.checkBoxTrailer.setOnClickListener {
            if( binding.imgTrailer.isSelected){
                binding.imgTrailer.isSelected = false
                binding.txtTrailer.setTextColor(this.resources.getColor(R.color.heading_black))
            }else{
                binding.imgTrailer.isSelected = true
                binding.txtTrailer.setTextColor(this.resources.getColor(R.color.dark_blue))
            }

        }
        binding.editOrigin.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType,"origin")
            navigationUtils.navigateForActivityResult(
                intent = searchCityIntent(this@BasicDetailsActivity),
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
                            binding.editOrigin.setText(city.cityName().trim())
                        }

                    }
                }
                REQCODE_DESTINATION_SELECT_CITY -> {
                    if(data != null) {
                        val type = data.getStringExtra(CityType)
                        val city = data.getSerializableExtra("City") as CityModel
                        if(type =="origin") {
                            binding.editOrigin.setText(city.cityName().trim())
                        }

                    }
                }


        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemClicked(item: CityModel) {
        TODO("Not yet implemented")
    }
}


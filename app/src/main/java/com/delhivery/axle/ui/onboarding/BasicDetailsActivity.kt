package com.delhivery.axle.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBasicDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.searchcitystate.CityType
import com.delhivery.axle.ui.searchcitystate.HaveOldDestinations
import com.delhivery.axle.ui.searchcitystate.SelectedData
import com.delhivery.axle.ui.searchcitystate.searchCityIntent
import com.delhivery.axle.ui.searchcitystate.searchOriginCityIntent
import com.delhivery.axle.ui.searchcitystate.selectedCityStates
import com.delhivery.axle.utils.EVENT_SUBMITTED_ROUTES_TRUCKS
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TTL
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.StepKey
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class BasicDetailsActivity: BaseActivity<ActivityBasicDetailsBinding, BasicDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#181818")
    }

    @Inject
   lateinit var userPrefs:UserPrefs

    var startTime: Long = 0
    var endTime: Long = 0

    override fun getViewModelClass() = BasicDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_basic_details

    override fun requireConnection() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.progressStepLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationUtils.showProgressSteps(binding.progressStepLayout, 1)
        startTime = System.currentTimeMillis()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
                finishAffinity()
            }
        })
        binding.checkBoxOpenBody.setOnClickListener {

            if( binding.imgOpenTruck.isSelected){
                binding.imgOpenTruck.isSelected = false
                binding.checkBoxOpenBody.isSelected =false
                binding.txtOpenBody.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
                viewModel.selectedTrucks.remove("open")
            }else{
                binding.imgOpenTruck.isSelected = true
                binding.checkBoxOpenBody.isSelected =true
                binding.txtOpenBody.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
                viewModel.selectedTrucks.add("open")
            }
            enableSubmit()
        }
        binding.checkBoxContainer.setOnClickListener {
            if( binding.imgContainer.isSelected){
                binding.imgContainer.isSelected = false
                binding.checkBoxContainer.isSelected =false
                binding.txtContainer.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
                viewModel.selectedTrucks.remove("closed")
            }else{
                binding.imgContainer.isSelected = true
                binding.checkBoxContainer.isSelected =true
                binding.txtContainer.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
                viewModel.selectedTrucks.add("closed")
            }
            enableSubmit()
        }
        binding.checkBoxTrailer.setOnClickListener {
            if( binding.imgTrailer.isSelected){
                binding.imgTrailer.isSelected = false
                binding.checkBoxTrailer.isSelected =false
                binding.txtTrailer.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
                viewModel.selectedTrucks.remove("trailer")
            }else{
                binding.imgTrailer.isSelected = true
                binding.checkBoxTrailer.isSelected =true
                binding.txtTrailer.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
                viewModel.selectedTrucks.add("trailer")
            }
            enableSubmit()
        }
        binding.editOrigin.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType,"origin")
            if(viewModel.selectedOrigin!=null){
            bundle.putString(SelectedData,viewModel.selectedOrigin!!.city)
            }
            navigationUtils.navigateForActivityResult(
                intent = searchOriginCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_SELECT_CITY, extras = bundle
            )
        }
        binding.editDestination.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType,"destination")
            if(binding.editDestination.text.isNullOrEmpty()){
                bundle.putBoolean(HaveOldDestinations,false)
            }else{
                bundle.putBoolean(HaveOldDestinations,true)
            }
            navigationUtils.navigateForActivityResult(
                intent = searchCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_DESTINATION_SELECT_CITY, extras = bundle
            )
        }

        binding.btnSubmitDetails.setOnClickListener{
            viewModel.updateUserDetails()
        }

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.trackEvent(
                    EVENT_SUBMITTED_ROUTES_TRUCKS,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )

                val bundle = Bundle()
                bundle.putInt(StepKey,0)
                navigationUtils.navigateKyc(this,true,bundle)
            } else {
                uiUtils.showSnackbar("Update Failed, Please try again")
            }
        })

        if(!userPrefs.getLanesPreference().isNullOrEmpty()){
            if(userPrefs.getLanesPreference()!!.isNotEmpty()){
                selectedCityStates = ArrayList<CityModel>()
                for((i,item) in userPrefs.getLanesPreference()!!.withIndex()){
                    if(item!=null){
                    if(i==0){
                        viewModel.selectedOrigin = CityModel(item!!.origin.city,item.origin.orion_db_city_code,"","",item.origin.type?:"city")
                    }
                    if(viewModel.selectedOrigin!!.city.equals(item!!.origin.city)){
                        val cityModel = CityModel(item.destination.state,item.destination.stateId,"",item.destination.state,item.destination.type?:"state")
                        selectedCityStates.add(cityModel)
                    }
                }
                binding.editOrigin.setText(viewModel.selectedOrigin!!.cityName().trim())
                }
                val citiesNames = ArrayList<String>()
                for(item in selectedCityStates){
                    citiesNames.add(item.cityName())
                }
                binding.editDestination.setText(citiesNames.joinToString(separator = ", "))

                if(userPrefs.truckTypes!=null){
                    if(userPrefs.truckTypes!!.contains("open")){
                        binding.checkBoxOpenBody.callOnClick()
                    }
                    if(userPrefs.truckTypes!!.contains("closed")){
                        binding.checkBoxContainer.callOnClick()
                    }
                    if(userPrefs.truckTypes!!.contains("trailer")){
                        binding.checkBoxTrailer.callOnClick()
                    }
                }
                enableSubmit()
            }

        }

     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQCODE_SELECT_CITY -> {
                    if(data != null) {
                        val type = data.getStringExtra(CityType)
                        val city = data.getSerializable("City", CityModel::class.java)
                        if(type =="origin") {
                            viewModel.selectedOrigin = city
                            binding.editOrigin.setText(city.cityName().trim())
                            enableSubmit()
                        }

                    }
                }
                REQCODE_DESTINATION_SELECT_CITY -> {
                    if(data != null) {
                        val type = data.getStringExtra(CityType)
                        val cities: ArrayList<CityModel> =
                            data.getSerializable("City",ArrayList<CityModel>().javaClass)
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

    /*override fun onBackPressed() {
        super.onBackPressed()
        finish()
        finishAffinity()
    }*/
}


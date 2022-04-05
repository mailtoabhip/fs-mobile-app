package com.delhivery.axle.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBasicDetailsBinding
import com.delhivery.axle.databinding.ActivityOnboardingBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.ui.base.BaseActivity
import android.app.Activity

import android.content.Intent
import com.delhivery.axle.utils.REQCODE_SELECT_CITY


class BasicDetailsActivity: BaseActivity<ActivityBasicDetailsBinding, BasicDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#181818")
    }

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
       /* binding.editOrigin.setOnClickListener(

        )*/
     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQCODE_SELECT_CITY -> {
                    // Do something if success / failed
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    }
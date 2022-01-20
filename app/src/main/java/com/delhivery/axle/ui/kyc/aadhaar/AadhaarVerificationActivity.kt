package com.delhivery.axle.ui.kyc.aadhaar

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVerifyAadharBinding
import com.delhivery.axle.ui.base.BaseActivity

class AadhaarVerificationActivity  : BaseActivity<ActivityVerifyAadharBinding, AadhaarVerificationViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    override fun getViewModelClass() = AadhaarVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_aadhar

    override fun requireConnection() = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.editAadhaar.apply {
            lengthAction(9){

            }

            lengthAction(10) {

            }

        }



        viewModel.errorLiveData.observe(
            this, Observer {
                it?.let { error ->

                }
            }
        )
    }
}

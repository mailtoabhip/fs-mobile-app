package com.delhivery.axle.ui.kyc.aadhaar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVerifyAadharBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity

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
            lengthAction(14){
                  binding.btnVerifyAadhaar.isEnabled = true
            }
            lengthAction(13){
                binding.btnVerifyAadhaar.isEnabled = false
            }
        }

        binding.btnVerifyAadhaar.setOnClickListener {
            navigationUtils.navigate(addressVerificationIntent(this), false)
        }
        viewModel.errorLiveData.observe(
            this, Observer {
                it?.let { error ->

                }
            }
        )
    }
}

fun addressVerificationIntent(
    context: Context
) = Intent(context, CommunicationAddressActivity::class.java).apply {
}

package com.delhivery.axle.ui.kyc.address

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityCommunicationAddressBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.setup

class CommunicationAddressActivity  : BaseActivity<ActivityCommunicationAddressBinding, CommunicationAddressViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    var flatFilled = false
    var areaFilled = false
    var cityFilled = false
    var pincodeFilled = false
    var proofTypeFilled = false
    var docUploadProof = true
    override fun getViewModelClass() = CommunicationAddressViewModel::class.java

    override fun layoutId() = R.layout.activity_communication_address

    override fun requireConnection() = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* Address Proof */
        binding.spinnerProof.setup(R.array.array_address__proof_type) { p, v ->
            if(p>0){
                proofTypeFilled = true
                enableSubmitButton()
            }else{
                proofTypeFilled =false
                enableSubmitButton()
            }
        }

        binding.btnSubmitDetails.setOnClickListener {

            if(pincodeFilled){
              viewModel.documentProofType =  binding.spinnerProof.selectedItem.toString()
            }
            viewModel.addNewAddress()
          //  navigationUtils.navigate(businessVerificationIntent(this),false)

        }

        //check length and enable/disable submit button
        binding.editCity.lengthAction(3){
            cityFilled = true
            enableSubmitButton()
        }
        binding.editCity.lengthAction(2){
            cityFilled = false
            enableSubmitButton()
        }
        binding.editArea.lengthAction(3){
            areaFilled = true
            enableSubmitButton()
        }
        binding.editArea.lengthAction(2){
            areaFilled = false
            enableSubmitButton()
        }
        binding.editFlat.lengthAction(3){
            flatFilled = true
            enableSubmitButton()
        }
        binding.editFlat.lengthAction(2){
            flatFilled = false
            enableSubmitButton()
        }
        binding.editPincode.lengthAction(6){
            pincodeFilled = true
            enableSubmitButton()
        }
        binding.editPincode.lengthAction(5){
            pincodeFilled = false
            enableSubmitButton()
        }
        viewModel.addAddressLiveData.observe(this, Observer {
            if (it) {
              //  startActivity(gstIntent(this))
                navigationUtils.navigate(businessVerificationIntent(this),false)
                finish()
            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
            }
        })
 }

    private fun enableSubmitButton(){
        binding.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
    }

    fun businessVerificationIntent(
        context: Context
    ) = Intent(context, BusinessVerificationActivity::class.java).apply {
    }
}
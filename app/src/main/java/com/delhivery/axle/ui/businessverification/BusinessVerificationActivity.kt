package com.delhivery.axle.ui.businessverification

import android.os.Bundle
import android.widget.RadioGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBusinessVerificationBinding
import com.delhivery.axle.ui.base.BaseActivity


class BusinessVerificationActivity : BaseActivity<ActivityBusinessVerificationBinding,BusinessVerificationViewModel>() {
    val rg :RadioGroup = RadioGroup(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rg.addView(binding.textTruck)
        rg.addView(binding.textLR)

        rg.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.text_truck -> {
                    viewModel.selected.postValue(true)

                }
                R.id.text_LR -> {
                    viewModel.selected.postValue(false)

                }
            }
        })

        binding.btnVerifyBusiness.setOnClickListener {


        }

    }

    override fun getViewModelClass()= BusinessVerificationViewModel::class.java

    override fun layoutId()=R.layout.activity_business_verification

    override fun requireConnection()= false

}
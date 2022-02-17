package com.delhivery.axle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBankDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity

class BankDetailsActivity : BaseActivity<ActivityBankDetailsBinding, HomeProfileViewModel>() {

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_bank_details

    override fun requireConnection() = true

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Bank Details"
    }

}
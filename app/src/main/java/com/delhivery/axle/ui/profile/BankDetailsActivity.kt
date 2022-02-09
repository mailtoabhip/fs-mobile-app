package com.delhivery.axle.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBankDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity

class BankDetailsActivity : BaseActivity<ActivityBankDetailsBinding, BankDetailsViewModel>() {

    override fun getViewModelClass() = BankDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_bank_details

    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_details)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Bank Details"
    }


}
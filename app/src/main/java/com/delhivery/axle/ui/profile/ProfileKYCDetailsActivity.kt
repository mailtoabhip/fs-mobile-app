package com.delhivery.axle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityProfileKycDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity

class ProfileKYCDetailsActivity : BaseActivity<ActivityProfileKycDetailsBinding, ProfileKYCDetailsViewModel>(){

    override fun getViewModelClass() = ProfileKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_kyc_details

    override fun requireConnection() = true

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "KYC Details"
    }


}

fun profileKYCDetailsIntent(
    context: Context
) = Intent( context, ProfileKYCDetailsActivity::class.java)
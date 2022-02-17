package com.delhivery.axle.ui.profile.kycdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityProfileKycDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.profile.kycdetails.fragments.ProfileKYCFragmentType
import com.delhivery.axle.ui.profile.kycdetails.fragments.ProfileKYCFragmentsAdapter

class ProfileKYCDetailsActivity : BaseActivity<ActivityProfileKycDetailsBinding, ProfileKYCDetailsViewModel>(){

    override fun getViewModelClass() = ProfileKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_kyc_details

    override fun requireConnection() = true

    /*  fragments pager adapter */
    private lateinit var pagerAdapter: ProfileKYCFragmentsAdapter


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "KYC Details"
        pagerAdapter = ProfileKYCFragmentsAdapter(supportFragmentManager)

        binding.viewpager.apply {
            offscreenPageLimit = ProfileKYCFragmentType.count()
            adapter = pagerAdapter
            }

        binding.kycTabLayout.setupWithViewPager(binding.viewpager)
    }


}

fun profileKYCDetailsIntent(
    context: Context
) = Intent( context, ProfileKYCDetailsActivity::class.java)
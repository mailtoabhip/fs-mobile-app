package com.delhivery.axle.ui.profile.kycdetails

import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityProfileKycDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.profile.kycdetails.fragments.ProfileKYCFragmentType
import com.delhivery.axle.ui.profile.kycdetails.fragments.ProfileKYCFragmentsAdapter
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject


class ProfileKYCDetailsActivity : BaseActivity<ActivityProfileKycDetailsBinding, ProfileKYCDetailsViewModel>(){

    override fun getViewModelClass() = ProfileKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_kyc_details

    override fun requireConnection() = true

    @Inject lateinit var userPrefs: UserPrefs

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

        for (i in 0 until binding.kycTabLayout.getTabCount()) {
            val tab: TabLayout.Tab? = binding.kycTabLayout.getTabAt(i)
            if(userPrefs.verificationStatus.equals("failed") && userPrefs.noOfVerificationIssues.isNotNullOrEmpty()) {
                tab?.customView = pagerAdapter.getTabView(i, this, null)
            }else{
                tab?.customView = pagerAdapter.getTabView(i, this, null)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigationUtils.navigate(MyProfileActivity::class.java,true)
    }


}

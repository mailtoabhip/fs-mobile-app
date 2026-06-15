package com.dfd.delfin.ui.profile.kycdetails

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityProfileKycDetailsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.profile.MyProfileActivity
import com.dfd.delfin.ui.profile.kycdetails.fragments.ProfileKYCFragmentType
import com.dfd.delfin.ui.profile.kycdetails.fragments.ProfileKYCFragmentsAdapter
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.android.material.tabs.TabLayout
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject


class ProfileKYCDetailsActivity : BaseActivity<ActivityProfileKycDetailsBinding, ProfileKYCDetailsViewModel>(){

    override fun getViewModelClass() = ProfileKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_kyc_details

    override fun requireConnection() = true

    @Inject lateinit var userPrefs: UserPrefs

    /*  fragments pager adapter */
    private lateinit var pagerAdapter: ProfileKYCFragmentsAdapter

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("ProfileKYCDetailsActivity_SetupTime")
        activitySetupTrace?.start()
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        
        /* Handle window insets for edge-to-edge display (API 35+) - Apply BEFORE setSupportActionBar */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationUtils.navigate(MyProfileActivity::class.java,true)
                finish()
            }
        })

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

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

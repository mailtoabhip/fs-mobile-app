package com.delhivery.axle.ui.paymentdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVendorPolicyBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.EVENT_ACCEPT_VENDOR_POLICY
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TTL
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class VendorPolicyActivity : BaseActivity<ActivityVendorPolicyBinding, PaymentDetailsViewModel>() {

    @Inject
    lateinit var userPrefs: UserPrefs
    var startTime: Long = 0
    var endTime: Long = 0

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        startTime = System.currentTimeMillis()

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navigationUtils.navigate(PaymentDetailsActivity::class.java, true)
                finish()
            }
        })
        binding.vendorPolicyLink.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://orion.delhivery.com/#/paymentterms"))
            startActivity(browserIntent)
        }

        viewModel.vendorUserUpdateLiveData.observe(this, Observer {
            if (it) {
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_ACCEPT_VENDOR_POLICY,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )
                userPrefs.vendorPolicyAccepted = true
                userPrefs.verificationStatus="pending"
                navigationUtils.showKycSubmittedDialog()
            }
        })
        binding.buttonIAgree.setOnClickListener {
            viewModel.updateUserDetailsForVendorPolicy()
        }

    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        navigationUtils.navigate(PaymentDetailsActivity::class.java, true)
    }*/

    override fun getViewModelClass() = PaymentDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_vendor_policy

    override fun requireConnection() = true
}
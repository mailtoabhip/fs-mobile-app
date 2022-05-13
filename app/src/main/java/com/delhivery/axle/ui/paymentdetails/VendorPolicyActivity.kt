package com.delhivery.axle.ui.paymentdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVendorPolicyBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class VendorPolicyActivity : BaseActivity<ActivityVendorPolicyBinding, PaymentDetailsViewModel>() {

    @Inject
    lateinit var userPrefs: UserPrefs

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.vendorPolicyLink.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://orion.delhivery.com/#/paymentterms"))
            startActivity(browserIntent)
        }

        viewModel.vendorUserUpdateLiveData.observe(this, Observer {
            if (it) {
                userPrefs.vendorPolicyAccepted = true
                navigationUtils.showKycSubmittedDialog()
            }
        })
        binding.buttonIAgree.setOnClickListener {
            viewModel.updateUserDetailsForVendorPolicy()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigationUtils.navigate(PaymentDetailsActivity::class.java, true)
    }

    override fun getViewModelClass() = PaymentDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_vendor_policy

    override fun requireConnection() = true
}
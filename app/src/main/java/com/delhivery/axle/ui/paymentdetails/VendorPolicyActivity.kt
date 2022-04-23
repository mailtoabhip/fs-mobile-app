package com.delhivery.axle.ui.paymentdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVendorPolicyBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.activity_vendor_policy.*
import javax.inject.Inject

class VendorPolicyActivity : BaseActivity<ActivityVendorPolicyBinding, PaymentDetailsViewModel>() {

       @Inject
       lateinit var userPrefs: UserPrefs
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding.webView.webViewClient = WebViewClient()
          binding.webView.settings.javaScriptEnabled = true
        }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    binding.webView.settings.setSupportZoom(true)
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)


    binding.vendorPolicyLink.setOnClickListener {
      val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://orion.delhivery.com/#/paymentterms"))
      startActivity(browserIntent)
    }
    val pdf = "https://orion.delhivery.com/assets/orion_vendor_policy.pdf"
    binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$pdf")

    viewModel.vendorUserUpdateLiveData.observe(this, Observer {
      if(it) {
        userPrefs.vendorPolicyAccepted = true
        navigationUtils.showKycSubmittedDialog()
      }
    })
    binding.buttonIAgree.setOnClickListener{
      viewModel.updateUserDetailsForVendorPolicy()
    }

  }



        override fun onBackPressed() {
            if (webView.canGoBack())
                webView.goBack()
            else
                super.onBackPressed()
        }

    override fun getViewModelClass() = PaymentDetailsViewModel::class.java

    override fun layoutId()= R.layout.activity_vendor_policy

    override fun requireConnection()=true
}
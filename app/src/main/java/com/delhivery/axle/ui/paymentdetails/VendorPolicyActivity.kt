package com.delhivery.axle.ui.paymentdetails

import android.os.Bundle
import android.webkit.WebViewClient
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

            binding.webView.settings.setSupportZoom(true)

            val pdf = "https://icseindia.org/document/sample.pdf"
            binding.webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdf")
          //  uiUtils.showProgress()
           if(binding.webView.progress==25){
             uiUtils.hideProgress()
           }

          binding.buttonUploadAgain.setOnClickListener{
            //call update user api first
            userPrefs.vendorPolicyAccepted = true
            navigationUtils.showKycSubmittedDialog()
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
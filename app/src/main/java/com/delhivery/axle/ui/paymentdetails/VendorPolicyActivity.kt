package com.delhivery.axle.ui.paymentdetails

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityPaymentDetailsBinding
import com.delhivery.axle.databinding.ActivityVendorPolicyBinding
import com.delhivery.axle.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_vendor_policy.*

class VendorPolicyActivity : BaseActivity<ActivityVendorPolicyBinding, PaymentDetailsViewModel>() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding.webView.webViewClient = WebViewClient()

            binding.webView.loadUrl("https://www.google.com/")

            binding.webView.settings.javaScriptEnabled = true

            binding.webView.settings.setSupportZoom(true)
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
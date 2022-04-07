package com.delhivery.axle.ui.paymentdetails

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityIdentityVerificationBinding
import com.delhivery.axle.databinding.ActivityPaymentDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationViewModel
import com.delhivery.axle.utils.DialogUtilsInterface
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class PaymentDetailsActivity : BaseActivity<ActivityPaymentDetailsBinding, PaymentDetailsViewModel>() {

    @Inject
    lateinit var userPrefs: UserPrefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        viewModel.accountHolderText.observe(this, Observer {
            if(userPrefs.userName.equals(it,true)){
                binding.paymentError.visibility= View.GONE
                binding.nameDeclaration.visibility=View.GONE
            }else{
                binding.paymentError.visibility= View.VISIBLE
                binding.nameDeclaration.visibility=View.VISIBLE

            }
        })

// 194c uploa condition

        binding.btnSubmit.setOnClickListener {
           dialogUtils.showWebViewDialog("https://www.geeksforgeeks.org/")
        }


    }


    override fun layoutId() = R.layout.activity_payment_details

    override fun requireConnection(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getViewModelClass()= PaymentDetailsViewModel::class.java
}
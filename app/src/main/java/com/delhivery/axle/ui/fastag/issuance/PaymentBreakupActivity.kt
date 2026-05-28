package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityPaymentBreakupBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.wallet.AddMoneyDialogFragment

class PaymentBreakupActivity : BaseActivity<ActivityPaymentBreakupBinding, PaymentBreakupViewModel>() {

    override fun getViewModelClass() = PaymentBreakupViewModel::class.java
    override fun layoutId() = R.layout.activity_payment_breakup
    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()

        // TODO: Replace with actual API call
        viewModel.fetchPaymentBreakup()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Payment Breakup"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupClickListeners() {
        binding.btnProceedToPay.setOnClickListener {
            // Disabled state — wallet insufficient
        }

        binding.tvAddMoney.setOnClickListener {
            showAddMoneyBottomSheet()
        }

        binding.slideToConfirm.setOnSlideCompleteListener {
            // Payment deducted from wallet — show success bottom sheet
            showPaymentSuccessBottomSheet()
        }
    }

    private fun showPaymentSuccessBottomSheet() {
        PaymentSuccessBottomSheet.newInstance {
            val intent = Intent(this, FastagCollectionActivity::class.java)
            startActivity(intent)
        }.show(supportFragmentManager, PaymentSuccessBottomSheet.TAG)
    }

    private fun showAddMoneyBottomSheet() {
        AddMoneyDialogFragment.newInstance(
            redirectUrl = "https://www.delhivery.com/",
            onPaymentResult = { success ->
                if (success) {
                    // Refresh wallet balance after successful payment
                    viewModel.fetchPaymentBreakup()
                }
            }
        ).show(supportFragmentManager, "AddMoneyDialog")
    }
}

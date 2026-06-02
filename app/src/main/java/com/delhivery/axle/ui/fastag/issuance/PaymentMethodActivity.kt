package com.delhivery.axle.ui.fastag.issuance

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityPaymentMethodBinding
import dagger.android.support.DaggerAppCompatActivity

class PaymentMethodActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityPaymentMethodBinding
    private var selectedMethod: PaymentType = PaymentType.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_method)
        binding.hasSelection = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupSelection()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Payment Method"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupSelection() {
        binding.cardFullPayment.setOnClickListener { selectFullPayment() }
        binding.rbFullPayment.setOnClickListener { selectFullPayment() }

        binding.cardSecurityDeposit.setOnClickListener { selectSecurityDeposit() }
        binding.rbSecurityDeposit.setOnClickListener { selectSecurityDeposit() }
    }

    private fun selectFullPayment() {
        selectedMethod = PaymentType.FULL_PAYMENT
        binding.rbFullPayment.isChecked = true
        binding.rbSecurityDeposit.isChecked = false
        binding.hasSelection = true
    }

    private fun selectSecurityDeposit() {
        selectedMethod = PaymentType.SECURITY_DEPOSIT
        binding.rbSecurityDeposit.isChecked = true
        binding.rbFullPayment.isChecked = false
        binding.hasSelection = true
    }

    private fun setupClickListeners() {
        binding.bottomButtons.btnPrimary.text = "Proceed"
        binding.bottomButtons.btnSecondary.text = "Cancel"

        binding.bottomButtons.btnPrimary.setOnClickListener {
            if (selectedMethod != PaymentType.NONE) {
                // TODO: Navigate to payment gateway / next step
            }
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            finish()
        }
    }

    enum class PaymentType {
        NONE, FULL_PAYMENT, SECURITY_DEPOSIT
    }
}

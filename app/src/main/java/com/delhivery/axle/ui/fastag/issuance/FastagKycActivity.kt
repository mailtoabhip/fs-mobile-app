package com.delhivery.axle.ui.fastag.issuance

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagKycBinding
import com.delhivery.axle.ui.common.OtpBottomSheetFragment
import dagger.android.support.DaggerAppCompatActivity

class FastagKycActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityFastagKycBinding
    private var isOtherKycExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fastag_kyc)
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
        supportActionBar?.title = "KYC"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupSelection() {
        // Full KYC is disabled
        binding.cardFullKyc.isClickable = false
        binding.cardFullKyc.isFocusable = false

        // Other KYC options dropdown toggle
        binding.otherKycHeader.setOnClickListener {
            toggleOtherKycOptions()
        }

        // eKYC selection
        binding.rbEkyc.setOnClickListener { selectEkyc() }
        binding.ekycContent.setOnClickListener { selectEkyc() }

        // Express KYC selection
        binding.rbExpressKyc.setOnClickListener { selectExpressKyc() }
        binding.expressKycContent.setOnClickListener { selectExpressKyc() }
    }

    private fun toggleOtherKycOptions() {
        isOtherKycExpanded = !isOtherKycExpanded
        binding.expandedKycContent.visibility = if (isOtherKycExpanded) View.VISIBLE else View.GONE
        binding.ivDropdown.rotation = if (isOtherKycExpanded) 180f else 0f
    }

    private fun selectEkyc() {
        binding.rbEkyc.isChecked = true
        binding.rbExpressKyc.isChecked = false
        binding.hasSelection = true
    }

    private fun selectExpressKyc() {
        binding.rbExpressKyc.isChecked = true
        binding.rbEkyc.isChecked = false
        binding.hasSelection = true
    }

    private fun setupClickListeners() {
        binding.bottomButtons.btnPrimary.text = "Proceed"
        binding.bottomButtons.btnSecondary.text = "Cancel"

        binding.bottomButtons.btnPrimary.setOnClickListener {
            if (binding.rbExpressKyc.isChecked) {
                showOtpBottomSheet()
            } else if (binding.rbEkyc.isChecked) {
                // TODO: Navigate to eKYC flow
            }
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            finish()
        }
    }

    private fun showOtpBottomSheet() {
        val otpSheet = OtpBottomSheetFragment.newInstance(
            maskedNumber = "XXXXXX7870",
            onSubmit = { otp ->
                // OTP verified successfully — show document verification bottom sheet
                showDocumentVerificationBottomSheet()
            },
            onResend = {
                // TODO: Resend OTP via API
            }
        )
        otpSheet.show(supportFragmentManager, OtpBottomSheetFragment.TAG)
    }

    private fun showDocumentVerificationBottomSheet() {
        val bottomSheet = DocumentVerificationBottomSheet.newInstance()
        bottomSheet.show(supportFragmentManager, DocumentVerificationBottomSheet.TAG)
    }
}

package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.ActivityFastagKycBinding
import com.delhivery.axle.ui.common.OtpBottomSheetFragment
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class FastagKycActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var dialogUtils: com.delhivery.axle.utils.DialogUtils

    @Inject
    lateinit var userPrefs: com.delhivery.axle.utils.prefs.UserPrefs

    private lateinit var binding: ActivityFastagKycBinding
    private lateinit var viewModel: FastagKycViewModel
    private var isOtherKycExpanded = false

    // Track available KYC types from API
    private var hasFullKyc = false
    private var hasExpressKyc = false
    private var hasEkyc = false

    private var bankCode = "IDFC" // Will be set from API response
    private var journeyId = ""
    private var selectedKycType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fastag_kyc)
        binding.hasSelection = false
        viewModel = ViewModelProvider(this, viewModelFactory)[FastagKycViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupSelection()
        setupClickListeners()
        observeViewModel()

        // Fetch KYC types from API
        viewModel.fetchKycTypes(bankCode)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "KYC"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun observeViewModel() {
        viewModel.kycTypesState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    bankCode = data.bankCode
                    val types = data.kycTypes.map { it.kycType }

                    hasFullKyc = types.contains("FULL_KYC")
                    hasExpressKyc = types.contains("MTS")
                    hasEkyc = types.contains("EKYC")

                    updateKycOptionsVisibility()
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    dialogUtils.showErrorDialog(message)
                }
            }
        }

        viewModel.kycInitiateState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading on button
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    journeyId = data.journeyId
                    when (data.nextStage) {
                        "OTPVerification", "MobileOTP" -> {
                            val mobileNumber = data.stageData?.mobileNumber
                                ?: data.mobileNumber
                                ?: userPrefs.phoneNumber
                                ?: ""
                            val masked = if (mobileNumber.length >= 4) {
                                "XXXXXX" + mobileNumber.takeLast(4)
                            } else "your registered number"
                            showOtpBottomSheet(masked)
                        }
                        "TagIssuance" -> {
                            startActivity(Intent(this, PaymentBreakupActivity::class.java))
                        }
                    }
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    dialogUtils.showErrorDialog(message)
                }
            }
        }

        viewModel.kycVerifyState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    // Dismiss OTP bottom sheet on success
                    val otpSheet = supportFragmentManager.findFragmentByTag(com.delhivery.axle.ui.common.OtpBottomSheetFragment.TAG)
                    (otpSheet as? com.delhivery.axle.ui.common.OtpBottomSheetFragment)?.dismiss()

                    when (data.nextStage) {
                        "TagIssuance" -> {
                            showIdentityVerificationSuccessBottomSheet()
                        }
                    }
                }

                is Resource.Failure -> {
                    val otpSheet = supportFragmentManager.findFragmentByTag(com.delhivery.axle.ui.common.OtpBottomSheetFragment.TAG)
                        as? com.delhivery.axle.ui.common.OtpBottomSheetFragment

                    if (resource.errorCode == 400) {
                        // Invalid OTP — show inline error on the bottom sheet
                        val errorMsg = resource.errorMessage ?: "Incorrect OTP"
                        otpSheet?.showOtpError(errorMsg)
                    } else {
                        // Server error — dismiss sheet and show error dialog
                        otpSheet?.dismiss()
                        val message = resource.errorMessage
                            ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                        dialogUtils.showErrorDialog(message)
                    }
                }
            }
        }
    }

    private fun updateKycOptionsVisibility() {
        // Full KYC card - always shown but greyed out
        binding.cardFullKyc.visibility = if (hasFullKyc) View.VISIBLE else View.GONE

        // Other options section - show if eKYC or Express KYC available
        binding.cardOtherKyc.visibility = if (hasEkyc || hasExpressKyc) View.VISIBLE else View.GONE

        // Individual options inside the dropdown
        binding.ekycContent.visibility = if (hasEkyc) View.VISIBLE else View.GONE
        binding.expressKycContent.visibility = if (hasExpressKyc) View.VISIBLE else View.GONE
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
        binding.bottomButtons.btnPrimary.isEnabled = true
        binding.bottomButtons.btnPrimary.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)
    }

    private fun selectExpressKyc() {
        binding.rbExpressKyc.isChecked = true
        binding.rbEkyc.isChecked = false
        binding.hasSelection = true
        binding.bottomButtons.btnPrimary.isEnabled = true
        binding.bottomButtons.btnPrimary.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)
    }

    private fun setupClickListeners() {
        binding.bottomButtons.btnPrimary.text = "Proceed"
        binding.bottomButtons.btnSecondary.text = "Cancel"

        // Disable Proceed until a KYC type is selected
        binding.bottomButtons.btnPrimary.isEnabled = false
        binding.bottomButtons.btnPrimary.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)

        binding.bottomButtons.btnPrimary.setOnClickListener {
            selectedKycType = when {
                binding.rbExpressKyc.isChecked -> "MTS"
                binding.rbEkyc.isChecked -> "EKYC"
                else -> return@setOnClickListener
            }
            android.util.Log.d("FastagKyc", "Initiating KYC: bankCode=$bankCode, kycType=$selectedKycType")
            viewModel.initiateKyc(bankCode, selectedKycType)
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            finish()
        }
    }

    private fun showOtpBottomSheet(maskedNumber: String) {
        val otpSheet = OtpBottomSheetFragment.newInstance(
            maskedNumber = maskedNumber,
            onSubmit = { otp ->
                viewModel.verifyAndCreateKyc(journeyId, otp, bankCode, selectedKycType)
            },
            onResend = {
                // Re-initiate KYC to resend OTP
                viewModel.initiateKyc(bankCode, selectedKycType)
            }
        )
        otpSheet.show(supportFragmentManager, OtpBottomSheetFragment.TAG)
    }

    private fun showIdentityVerificationSuccessBottomSheet() {
        val bottomSheet = DocumentVerificationBottomSheet.newInstance()
        bottomSheet.show(supportFragmentManager, DocumentVerificationBottomSheet.TAG)
    }
}

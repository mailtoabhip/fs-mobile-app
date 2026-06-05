package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.ActivityFastagKycBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.common.OtpBottomSheetFragment
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class FastagKycActivity : BaseActivity<ActivityFastagKycBinding, FastagKycViewModel>() {

    override fun getViewModelClass() = FastagKycViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_kyc
    override fun requireConnection() = true

    @Inject
    lateinit var userPrefs: UserPrefs

    private var isOtherKycExpanded = false

    private var hasFullKyc = false
    private var hasExpressKyc = false
    private var hasEkyc = false

    private var bankCode = "IDFC"
    private var journeyId = ""
    private var selectedKycType = ""
    private var salesCode = ""
    private var orderId = ""
    private var items: ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.lifecycleOwner = this
        binding.hasSelection = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupSelection()
        setupClickListeners()
        observeViewModel()

        salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        items = intent.getSerializableExtra(PaymentBreakupActivity.EXTRA_ITEMS) as? ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem>
            ?: arrayListOf()

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
                is Resource.Loading -> {}

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
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    journeyId = data.journeyId
                    when (data.nextStage) {
                        "OTPVerification", "MobileOTP" -> {
                            val mobileNumber = userPrefs.phoneNumber ?: ""
                            val masked = if (mobileNumber.length >= 4) {
                                "XXXXXX" + mobileNumber.takeLast(4)
                            } else "your registered number"
                            showOtpBottomSheet(masked)
                        }
                        "TagIssuance" -> {
                            startActivity(Intent(this, PaymentBreakupActivity::class.java).apply {
                                putExtra(PaymentBreakupActivity.EXTRA_SALES_CODE, salesCode)
                                putExtra(PaymentBreakupActivity.EXTRA_ORDER_ID, orderId)
                                putExtra(PaymentBreakupActivity.EXTRA_ITEMS, items)
                            })
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
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    val otpSheet = supportFragmentManager.findFragmentByTag(OtpBottomSheetFragment.TAG)
                    (otpSheet as? OtpBottomSheetFragment)?.dismiss()

                    when (data.nextStage) {
                        "TagIssuance" -> {
                            showIdentityVerificationSuccessBottomSheet()
                        }
                    }
                }

                is Resource.Failure -> {
                    val otpSheet = supportFragmentManager.findFragmentByTag(OtpBottomSheetFragment.TAG)
                        as? OtpBottomSheetFragment

                    if (resource.errorCode == 400) {
                        val errorMsg = resource.errorMessage ?: "Incorrect OTP"
                        otpSheet?.showOtpError(errorMsg)
                    } else {
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
        binding.cardFullKyc.visibility = if (hasFullKyc) View.VISIBLE else View.GONE
        binding.cardOtherKyc.visibility = if (hasEkyc || hasExpressKyc) View.VISIBLE else View.GONE
        binding.ekycContent.visibility = if (hasEkyc) View.VISIBLE else View.GONE
        binding.expressKycContent.visibility = if (hasExpressKyc) View.VISIBLE else View.GONE
    }

    private fun setupSelection() {
        // Full KYC is disabled
        binding.cardFullKyc.isClickable = false
        binding.cardFullKyc.isFocusable = false

        // eKYC is disabled
        binding.rbEkyc.isEnabled = false
        binding.ekycContent.isClickable = false
        binding.ekycContent.alpha = 0.5f

        // Expand the dropdown by default
        isOtherKycExpanded = true
        binding.expandedKycContent.visibility = View.VISIBLE
        binding.ivDropdown.rotation = 180f

        // Other KYC options dropdown toggle
        binding.otherKycHeader.setOnClickListener { toggleOtherKycOptions() }

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

        binding.bottomButtons.btnPrimary.isEnabled = false
        binding.bottomButtons.btnPrimary.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)

        binding.bottomButtons.btnPrimary.setOnClickListener {
            selectedKycType = when {
                binding.rbExpressKyc.isChecked -> "MTS"
                binding.rbEkyc.isChecked -> "EKYC"
                else -> return@setOnClickListener
            }
            viewModel.initiateKyc(bankCode, selectedKycType)
        }

        binding.bottomButtons.btnSecondary.setOnClickListener { finish() }
    }

    private fun showOtpBottomSheet(maskedNumber: String) {
        val otpSheet = OtpBottomSheetFragment.newInstance(
            maskedNumber = maskedNumber,
            onSubmit = { otp ->
                viewModel.verifyAndCreateKyc(journeyId, otp, bankCode, selectedKycType)
            },
            onResend = {
                viewModel.initiateKyc(bankCode, selectedKycType)
            }
        )
        otpSheet.show(supportFragmentManager, OtpBottomSheetFragment.TAG)
    }

    private fun showIdentityVerificationSuccessBottomSheet() {
        val bottomSheet = DocumentVerificationBottomSheet.newInstance(
            salesCode = salesCode,
            orderId = orderId
        )
        bottomSheet.show(supportFragmentManager, DocumentVerificationBottomSheet.TAG)
    }

    companion object {
        const val EXTRA_SALES_CODE = "extra_sales_code"
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
}

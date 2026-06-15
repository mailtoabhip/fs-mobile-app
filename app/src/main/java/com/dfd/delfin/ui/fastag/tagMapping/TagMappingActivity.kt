package com.dfd.delfin.ui.fastag.tagMapping

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dfd.delfin.R
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.databinding.ActivityTagMappingBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.common.OtpBottomSheetFragment
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

class TagMappingActivity : BaseActivity<ActivityTagMappingBinding, TagMappingViewModel>() {

    override fun getViewModelClass() = TagMappingViewModel::class.java
    override fun layoutId() = R.layout.activity_tag_mapping

    @Inject lateinit var userPrefs: UserPrefs

    private var selectedBarcode: String? = null
    private var lastTagId: String? = null
    private var barcodeList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupDropdown()
        setupContinueButton()
        observeViewModel()
        populateVehicleCard()
        fetchBarcodeLookup()
    }

    private fun populateVehicleCard() {
        binding.tvVehicleNumber.text = intent.getStringExtra(EXTRA_VEHICLE_NUMBER).orEmpty()
        binding.tvVehicleClass.text = intent.getStringExtra(EXTRA_VEHICLE_CLASS).orEmpty()
        val tagColor = intent.getStringExtra(EXTRA_TAG_COLOR).orEmpty()
        binding.tvTagColor.text = tagColor
        binding.tvProvider.text = intent.getStringExtra(EXTRA_BANK).orEmpty()
        binding.viewTagColorIndicator.setBackgroundColor(mapTagColor(tagColor))
    }

    private fun mapTagColor(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "ORANGE" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_green)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupDropdown() {
        binding.layoutDropdown.setOnClickListener {
            showBarcodeSelectionBottomSheet()
        }
    }

    private fun showBarcodeSelectionBottomSheet() {
        if (barcodeList.isEmpty()) {
            Toast.makeText(this, "No barcodes available", Toast.LENGTH_SHORT).show()
            return
        }
        val popup = android.widget.ListPopupWindow(this)
        popup.anchorView = binding.layoutDropdown
        popup.setAdapter(
            android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, barcodeList)
        )
        popup.width = binding.layoutDropdown.width
        popup.isModal = true

        // Arrow up when open
        binding.ivDropdownArrow.setImageResource(R.drawable.drop_down_arrow_up_iv)

        popup.setOnItemClickListener { _, _, position, _ ->
            selectedBarcode = barcodeList[position]
            onBarcodeSelected(barcodeList[position])
            popup.dismiss()
        }
        popup.setOnDismissListener {
            // Arrow down when closed
            binding.ivDropdownArrow.setImageResource(R.drawable.drop_dwn_arrow_down_iv)
        }
        popup.show()
    }

    private fun onBarcodeSelected(barcode: String) {
        binding.tvDropdownValue.text = barcode
        binding.tvDropdownValue.setTextColor(getColor(R.color.text_body_primary))
        enableContinueButton()
    }

    private fun enableContinueButton() {
        binding.btnContinue.isEnabled = true
        binding.btnContinue.setBackgroundResource(R.drawable.bg_tag_mapping_continue_enabled)
        binding.btnContinue.setTextColor(getColor(android.R.color.white))
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            val barcode = selectedBarcode ?: return@setOnClickListener
            val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
            viewModel.searchProductBarcode(journeyId, barcode.replace(" ", ""))
        }
    }

    private fun observeViewModel() {
        viewModel.barcodeLookupData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show progress indicator if needed
                }
                is Resource.Success -> {
                    resource.data?.let { response ->
                        val barcode = response.barcode
                        if (!barcode.isNullOrEmpty()) {
                            barcodeList = listOf(barcode)
                        }
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(this, resource.errorMessage ?: "Failed to fetch barcode data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.productBarcodeData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    uiUtils.showProgress()
                    binding.btnContinue.isEnabled = false
                }
                is Resource.Success -> {
                    uiUtils.hideProgress()
                    binding.btnContinue.isEnabled = true
                    resource.data?.let { response ->
                        // Capture tagId from first barcode item for generate-otp
                        lastTagId = response.barcodes?.firstOrNull()?.tagId
                        showMinimumBalanceBottomSheet()
                    }
                }
                is Resource.Failure -> {
                    uiUtils.hideProgress()
                    binding.btnContinue.isEnabled = true
                    Toast.makeText(this, resource.errorMessage ?: "Failed to search product barcode", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.generateOtpData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // OTP generation in progress
                }
                is Resource.Success -> {
                    // Dismiss minimum balance bottom sheet if showing
                    (supportFragmentManager.findFragmentByTag("MinimumBalanceBottomSheet")
                        as? com.google.android.material.bottomsheet.BottomSheetDialogFragment)?.dismiss()

                    val existingSheet = supportFragmentManager
                        .findFragmentByTag(OtpBottomSheetFragment.TAG)
                        as? OtpBottomSheetFragment
                    if (existingSheet != null && existingSheet.isAdded) {
                        existingSheet.clearOtp()
                    } else {
                        val phone = userPrefs.phoneNumber.orEmpty()
                        val maskedNumber = if (phone.length >= 4) {
                            "******" + phone.takeLast(4)
                        } else {
                            phone
                        }
                        showOtpBottomSheet(maskedNumber)
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(this, resource.errorMessage ?: "Failed to generate OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.issueTagData.observe(this) { resource ->
            val otpSheet = supportFragmentManager.findFragmentByTag(OtpBottomSheetFragment.TAG)
                as? OtpBottomSheetFragment
            when (resource) {
                is Resource.Loading -> {
                    otpSheet?.showLoading()
                }
                is Resource.Success -> {
                    otpSheet?.hideLoading()
                    otpSheet?.dismiss()
                    showTagMappingSuccessBottomSheet()
                }
                is Resource.Failure -> {
                    otpSheet?.hideLoading()
                    Toast.makeText(this, resource.errorMessage ?: "Failed to issue FASTag", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchBarcodeLookup() {
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: return
        val orderItemIdStr = intent.getStringExtra(EXTRA_ORDER_ITEM_ID) ?: return
        val orderItemId = orderItemIdStr.toIntOrNull() ?: return
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: return

        viewModel.fetchBarcodeLookup(orderId, orderItemId, vehicleClass)
    }

    private fun showMinimumBalanceBottomSheet() {
        val bottomSheet = MinimumBalanceBottomSheetFragment.newInstance {
            // On continue after acknowledging, call generate OTP
            callGenerateOtp()
        }
        bottomSheet.show(supportFragmentManager, "MinimumBalanceBottomSheet")
    }

    private fun showTagMappingSuccessBottomSheet() {
        val bottomSheet = TagMappingSuccessBottomSheetFragment.newInstance {
            val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
            val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
            val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
            val orderItemId = intent.getStringExtra(EXTRA_ORDER_ITEM_ID) ?: ""
            val navIntent = com.dfd.delfin.ui.fastag.tagAssignment.assign.kyv.KYVFastagImageUploadActivity.newIntent(
                this, vehicleNumber, journeyId, orderId, orderItemId
            )
            startActivity(navIntent)
            finish()
        }
        bottomSheet.show(supportFragmentManager, "TagMappingSuccessBottomSheet")
    }

    private fun callGenerateOtp() {
        val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
        val barcode = selectedBarcode?.replace(" ", "") ?: return
        val tagId = lastTagId ?: return
        viewModel.generateOtp(journeyId, barcode, tagId)
    }

    private fun showOtpBottomSheet(maskedNumber: String) {
        val otpSheet = com.dfd.delfin.ui.common.OtpBottomSheetFragment.newInstance(
            maskedNumber = maskedNumber,
            onSubmit = { otp ->
                callIssueTag(otp)
            },
            onResend = {
                callGenerateOtp()
            }
        )
        otpSheet.show(supportFragmentManager, com.dfd.delfin.ui.common.OtpBottomSheetFragment.TAG)
    }

    private fun callIssueTag(otp: String) {
        val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = (intent.getStringExtra(EXTRA_ORDER_ITEM_ID) ?: "").toIntOrNull() ?: -1
        val barcode = selectedBarcode?.replace(" ", "") ?: return
        viewModel.issueTag(journeyId, orderId, orderItemId, barcode, otp)
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_ORDER_ITEM_ID = "extra_item_id"
        const val EXTRA_VEHICLE_CLASS = "vehicle_class"
        const val EXTRA_JOURNEY_ID = "journey_id"
        const val EXTRA_VEHICLE_NUMBER = "vehicle_number"
        const val EXTRA_TAG_COLOR = "tag_color"
        const val EXTRA_BANK = "bank"
    }
}

package com.delhivery.axle.ui.fastag.tagMapping

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.ActivityTagMappingBinding
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class TagMappingActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityTagMappingBinding
    private lateinit var viewModel: TagMappingViewModel

    private var selectedBarcode: String? = null
    private var lastTagId: String? = null
    private var barcodeList: List<String> = listOf(
        "5274 9183 6402 1758 29",
        "6381 0274 5918 3640 17",
        "8472 1936 0541 2837 46",
        "9012 3456 7890 1234 56",
        "1122 3344 5566 7788 90"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTagMappingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, viewModelFactory)[TagMappingViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHeader()
        setupDropdown()
        setupContinueButton()
        observeViewModel()
        populateVehicleCard()
        fetchBarcodeLookup()
    }

    private fun populateVehicleCard() {
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: "MH01CA1234"
        binding.tvVehicleNumber.text = vehicleNumber
        // TODO: Tag color and provider will come from barcode API response
        binding.tvVehicleClass.text = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: "7"
        binding.tvTagColor.text = "Green"
        binding.tvProvider.text = "IDFC"
    }

    private fun setupHeader() {
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivHelp.setOnClickListener {
            // TODO: Handle help icon click
        }
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
        popup.setOnItemClickListener { _, _, position, _ ->
            selectedBarcode = barcodeList[position]
            onBarcodeSelected(barcodeList[position])
            popup.dismiss()
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
            val last4 = barcode.replace(" ", "").takeLast(4)
            viewModel.searchProductBarcode(journeyId, last4)
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
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.text = "Loading..."
                }
                is Resource.Success -> {
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.text = "Continue"
                    resource.data?.let { response ->
                        // Capture tagId from first barcode item for generate-otp
                        lastTagId = response.barcodes?.firstOrNull()?.tagId
                        showMinimumBalanceBottomSheet()
                    }
                }
                is Resource.Failure -> {
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.text = "Continue"
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
                    // Show OTP bottom sheet with masked number
                    // TODO: Replace with actual masked number from API or intent
                    val maskedNumber = "XXXXXX1234"
                    showOtpBottomSheet(maskedNumber)
                }
                is Resource.Failure -> {
                    Toast.makeText(this, resource.errorMessage ?: "Failed to generate OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.issueTagData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Tag issuance in progress
                }
                is Resource.Success -> {
                    // Dismiss OTP bottom sheet and show success
                    supportFragmentManager.findFragmentByTag(com.delhivery.axle.ui.common.OtpBottomSheetFragment.TAG)
                        ?.let { (it as? com.delhivery.axle.ui.common.OtpBottomSheetFragment)?.dismiss() }
                    resource.data?.let { response ->
                        showTagMappingSuccessBottomSheet()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(this, resource.errorMessage ?: "Failed to issue FASTag", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchBarcodeLookup() {
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: return
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, -1)
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: return

        if (orderItemId == -1) return

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
            // TODO: Navigate to KYV screen
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
        val otpSheet = com.delhivery.axle.ui.common.OtpBottomSheetFragment.newInstance(
            maskedNumber = maskedNumber,
            onSubmit = { otp ->
                callIssueTag(otp)
            },
            onResend = {
                callGenerateOtp()
            }
        )
        otpSheet.show(supportFragmentManager, com.delhivery.axle.ui.common.OtpBottomSheetFragment.TAG)
    }

    private fun callIssueTag(otp: String) {
        val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, -1)
        val barcode = selectedBarcode?.replace(" ", "") ?: return
        viewModel.issueTag(journeyId, orderId, orderItemId, barcode, otp)
    }

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_ORDER_ITEM_ID = "order_item_id"
        const val EXTRA_VEHICLE_CLASS = "vehicle_class"
        const val EXTRA_JOURNEY_ID = "journey_id"
        const val EXTRA_VEHICLE_NUMBER = "vehicle_number"
    }
}

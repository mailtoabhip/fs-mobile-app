package com.dfd.delfin.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.dfd.delfin.R
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.databinding.ActivityAddVehicleBinding
import com.dfd.delfin.ui.base.BaseActivity

class AddVehicleActivity : BaseActivity<ActivityAddVehicleBinding, AddVehicleViewModel>() {

    override fun getViewModelClass() = AddVehicleViewModel::class.java
    override fun layoutId() = R.layout.activity_add_vehicle
    override fun requireConnection() = true

    private var isVehicleEligible = false
    private var hasNavigatedFromCheck = false
    private var currentOrderId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.lifecycleOwner = this
        binding.truckNumber = ""

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupTextWatcher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Vehicle"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTextWatcher() {
        binding.etTruckNumber.addTextChangedListener { text ->
            isVehicleEligible = false
            hasNavigatedFromCheck = false

            val truckNumber = text?.toString()?.trim() ?: ""
            if (truckNumber.isEmpty()) {
                binding.tvError.visibility = android.view.View.GONE
                binding.etTruckNumber.setBackgroundResource(R.drawable.bg_edit_text_outline)
                binding.btnContinue.isEnabled = false
                binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
            } else if (isValidVehicleNumber(truckNumber)) {
                binding.tvError.visibility = android.view.View.GONE
                binding.etTruckNumber.setBackgroundResource(R.drawable.bg_edit_text_outline)
                binding.btnContinue.isEnabled = true
                binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)
            } else {
                binding.tvError.visibility = android.view.View.VISIBLE
                binding.etTruckNumber.setBackgroundResource(R.drawable.bg_edit_text_error)
                binding.btnContinue.isEnabled = false
                binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val truckNumber = binding.etTruckNumber.text?.toString()?.trim() ?: ""
            val normalized = truckNumber.uppercase().replace(" ", "").replace("-", "")
            if (isValidVehicleNumber(truckNumber)) {
                hasNavigatedFromCheck = false
                viewModel.checkVehicle(normalized)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.vehicleCheckState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    when (data.status) {
                        "ELIGIBLE" -> {
                            isVehicleEligible = true
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)

                            if (hasNavigatedFromCheck) return@observe

                            hasNavigatedFromCheck = true
                            val intent = Intent(this, SelectFasTagActivity::class.java).apply {
                                putExtra(SelectFasTagActivity.EXTRA_VRN, data.vrn)
                                putExtra(EXTRA_SALES_CODE, getIntent().getStringExtra(EXTRA_SALES_CODE) ?: "")
                                putExtra(EXTRA_CUSTOMER_NAME, getIntent().getStringExtra(EXTRA_CUSTOMER_NAME) ?: "")
                                if (data.npciVehicleClass != null) {
                                    putExtra(SelectFasTagActivity.EXTRA_VEHICLE_CLASS, data.npciVehicleClass)
                                }
                            }
                            startActivity(intent)
                        }
                        "HOTLISTED", "ALREADY_ISSUED" -> {
                            isVehicleEligible = false
                            binding.btnContinue.isEnabled = false
                            binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                            showVehicleBottomSheet(data)
                        }
                        else -> {
                            isVehicleEligible = false
                            binding.btnContinue.isEnabled = false
                            binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                            dialogUtils.showErrorDialog(data.message)
                        }
                    }
                }

                is Resource.Failure -> {
                    isVehicleEligible = false
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    dialogUtils.showErrorDialog(message)
                }
            }
        }

        viewModel.kycValidateState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    val salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
                    if (data.fastagCustomerExists) {
                        val items = arrayListOf(
                            com.dfd.delfin.api.request.PaymentBreakupItem(
                                vehicleClass = "VC5",
                                quantity = 1
                            )
                        )
                        val navIntent = Intent(this, PaymentBreakupActivity::class.java).apply {
                            putExtra(PaymentBreakupActivity.EXTRA_SALES_CODE, salesCode)
                            putExtra(PaymentBreakupActivity.EXTRA_PAYMENT_METHOD, "FULL_PAYMENT")
                            putExtra(PaymentBreakupActivity.EXTRA_ITEMS, items)
                            putExtra(PaymentBreakupActivity.EXTRA_ORDER_ID, currentOrderId)
                        }
                        startActivity(navIntent)
                    } else {
                        val navIntent = Intent(this, FastagKycActivity::class.java).apply {
                            putExtra(FastagKycActivity.EXTRA_SALES_CODE, salesCode)
                            putExtra(FastagKycActivity.EXTRA_ORDER_ID, currentOrderId)
                        }
                        startActivity(navIntent)
                    }
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    dialogUtils.showErrorDialog(message)
                }
            }
        }

        viewModel.createOrderState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    currentOrderId = data.orderId
                    viewModel.kycOnboardValidate("IDFC")
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    dialogUtils.showErrorDialog(message)
                }
            }
        }
    }

    private fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        val normalized = vehicleNumber
            .uppercase()
            .replace(" ", "")
            .replace("-", "")
        val regex = Regex("^(?:[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{4}|\\d{2}BH\\d{4}[A-Z]{2})$")
        return regex.matches(normalized)
    }

    private fun showVehicleBottomSheet(data: com.dfd.delfin.api.response.VehicleCheckResponse) {
        val existing = supportFragmentManager.findFragmentByTag(VehicleDetailsBottomSheet.TAG)
        if (existing != null) return

        VehicleDetailsBottomSheet.fromResponse(
            response = data,
            issuerPhone = "",
            onAction = if (data.eligible) {
                {
                    val salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
                    val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: ""
                    val request = com.dfd.delfin.api.request.CreateOrderRequest(
                        salesCode = salesCode,
                        customerName = customerName,
                        customerMobile = "",
                        vehicles = listOf(
                            com.dfd.delfin.api.request.OrderVehicleItem(
                                vrn = data.vrn,
                                vehicleClass = data.vehicleClass?.vehicleClass ?: data.npciVehicleClass ?: "VC5",
                                unitPrice = "100.00"
                            )
                        ),
                        totalAmount = "100.00",
                        idempotencyKey = "ORD-${java.util.UUID.randomUUID()}"
                    )
                    viewModel.createOrder(request)
                }
            } else null
        ).show(supportFragmentManager, VehicleDetailsBottomSheet.TAG)
    }

    companion object {
        const val EXTRA_TRUCK_NUMBER = "extra_truck_number"
        const val EXTRA_SALES_CODE = "extra_sales_code"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
    }
}

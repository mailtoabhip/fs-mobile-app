package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.ActivityAddVehicleBinding
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class AddVehicleActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var dialogUtils: com.delhivery.axle.utils.DialogUtils

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var viewModel: AddVehicleViewModel

    private var isVehicleEligible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_vehicle)
        binding.truckNumber = ""
        viewModel = ViewModelProvider(this, viewModelFactory)[AddVehicleViewModel::class.java]

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

    private var debounceJob: Job? = null
    private var lastCheckedVrn = ""

    private fun setupTextWatcher() {
        binding.etTruckNumber.addTextChangedListener { text ->
            isVehicleEligible = false
            binding.btnContinue.isEnabled = false
            binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)

            val truckNumber = text?.toString()?.trim() ?: ""
            val normalized = truckNumber.uppercase().replace(" ", "").replace("-", "")

            debounceJob?.cancel()

            if (isValidVehicleNumber(truckNumber) && normalized != lastCheckedVrn) {
                debounceJob = MainScope().launch {
                    delay(500) // 500ms debounce
                    lastCheckedVrn = normalized
                    viewModel.checkVehicle(normalized)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            // Continue is no longer needed as navigation happens from the bottom sheet
        }
    }

    private fun observeViewModel() {
        viewModel.vehicleCheckState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Optionally show a small loader on the input field
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    when (data.status) {
                        "ELIGIBLE" -> {
                            isVehicleEligible = true
                            binding.btnContinue.isEnabled = true
                            binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)

                            if (data.npciVehicleClass == null) {
                                // Vehicle class unknown — navigate to SelectFasTagActivity to pick one
                                val intent = Intent(this, SelectFasTagActivity::class.java).apply {
                                    putExtra(SelectFasTagActivity.EXTRA_VRN, data.vrn)
                                    putExtra(EXTRA_SALES_CODE, getIntent().getStringExtra(EXTRA_SALES_CODE) ?: "")
                                    putExtra(EXTRA_CUSTOMER_NAME, getIntent().getStringExtra(EXTRA_CUSTOMER_NAME) ?: "")
                                }
                                startActivity(intent)
                            } else {
                                // Vehicle class known — show bottom sheet
                                showVehicleBottomSheet(data)
                            }
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
                is Resource.Loading -> {
                    // TODO: Show loading
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    val salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
                    if (data.fastagCustomerExists) {
                        // Existing customer — skip KYC, go directly to payment
                        val items = arrayListOf(
                            com.delhivery.axle.api.request.PaymentBreakupItem(
                                vehicleClass = "VC5", // TODO: Use actual vehicle class
                                quantity = 1
                            )
                        )
                        val navIntent = Intent(this, PaymentBreakupActivity::class.java).apply {
                            putExtra(PaymentBreakupActivity.EXTRA_SALES_CODE, salesCode)
                            putExtra(PaymentBreakupActivity.EXTRA_PAYMENT_METHOD, "FULL_PAYMENT")
                            putExtra(PaymentBreakupActivity.EXTRA_ITEMS, items)
                        }
                        startActivity(navIntent)
                    } else {
                        // New customer — needs KYC
                        val navIntent = Intent(this, FastagKycActivity::class.java)
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

        // Create order observer — on success, call KYC validate to decide next screen
        viewModel.createOrderState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    // Order created — now check KYC status
                    viewModel.kycOnboardValidate("IDFC") // TODO: Use actual bank_code
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

        val regex = Regex(
            "^(?:[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{1,4}|\\d{2}BH\\d{4}[A-Z]{2})$"
        )
        return regex.matches(normalized)
    }

    // Kept for potential future use - navigation to SelectFasTagActivity
    // private fun navigateToSelectFasTag(truckNumber: String) {
    //     val intent = Intent(this, SelectFasTagActivity::class.java).apply {
    //         putExtra(EXTRA_TRUCK_NUMBER, truckNumber)
    //     }
    //     startActivity(intent)
    // }

    private fun showVehicleBottomSheet(data: com.delhivery.axle.api.response.VehicleCheckResponse) {
        // Prevent duplicate bottom sheets
        val existing = supportFragmentManager.findFragmentByTag(VehicleDetailsBottomSheet.TAG)
        if (existing != null) return

        VehicleDetailsBottomSheet.fromResponse(
            response = data,
            issuerPhone = "",
            onAction = if (data.eligible) {
                {
                    // Create order on confirm
                    val salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
                    val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: ""
                    val request = com.delhivery.axle.api.request.CreateOrderRequest(
                        salesCode = salesCode,
                        customerName = customerName,
                        customerMobile = "", // TODO: Pass from validate sales code API if available
                        vehicles = listOf(
                            com.delhivery.axle.api.request.OrderVehicleItem(
                                vrn = data.vrn,
                                vehicleClass = data.vehicleClass?.vehicleClass ?: data.npciVehicleClass ?: "VC5",
                                unitPrice = "100.00" // TODO: Get from API/config
                            )
                        ),
                        totalAmount = "100.00", // TODO: Calculate
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

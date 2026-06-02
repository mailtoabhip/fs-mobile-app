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
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.request.CreateOrderRequest
import com.delhivery.axle.api.response.VehicleClassData
import com.delhivery.axle.databinding.ActivitySelectFastagBinding
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SelectFasTagActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var dialogUtils: com.delhivery.axle.utils.DialogUtils

    private lateinit var binding: ActivitySelectFastagBinding
    private lateinit var viewModel: SelectFasTagViewModel
    private var adapter: VehicleClassAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_fastag)
        binding.hasSelection = false

        viewModel = ViewModelProvider(this, viewModelFactory)[SelectFasTagViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        observeCreateOrderAndKyc()

        viewModel.fetchVehicleClasses()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Select FASTag"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val selectedItem = adapter?.getSelectedItem() ?: return@setOnClickListener
            val salesCode = intent.getStringExtra(AddVehicleActivity.EXTRA_SALES_CODE) ?: ""
            val customerName = intent.getStringExtra(AddVehicleActivity.EXTRA_CUSTOMER_NAME) ?: ""
            val vrn = intent.getStringExtra(EXTRA_VRN) ?: ""

            val request = CreateOrderRequest(
                salesCode = salesCode,
                customerName = customerName,
                customerMobile = "", // TODO: Pass from validate sales code API if available
                vehicles = listOf(
                    com.delhivery.axle.api.request.OrderVehicleItem(
                        vrn = vrn,
                        vehicleClass = selectedItem.classId,
                        unitPrice = "100.00" // TODO: Get from API/config
                    )
                ),
                totalAmount = "100.00", // TODO: Calculate
                idempotencyKey = "ORD-${java.util.UUID.randomUUID()}"
            )
            viewModel.createOrder(request)
        }
    }

    private fun observeCreateOrderAndKyc() {
        viewModel.createOrderState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading
                }
                is Resource.Success -> {
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

        viewModel.kycValidateState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading
                }
                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    val salesCode = intent.getStringExtra(AddVehicleActivity.EXTRA_SALES_CODE) ?: ""
                    if (data.fastagCustomerExists) {
                        // Existing customer — skip KYC, go directly to payment
                        val items = arrayListOf(
                            com.delhivery.axle.api.request.PaymentBreakupItem(
                                vehicleClass = adapter?.getSelectedItem()?.classId ?: "",
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
    }

    private fun observeViewModel() {
        viewModel.vehicleClassesState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show shimmer/progress if needed
                }

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null) {
                        setupRecyclerView(data.vehicleClasses)
                    }
                }

                is Resource.Failure -> {
                    val message = if (resource.isNetworkError) {
                        "No internet connection. Please try again."
                    } else {
                        "Something went wrong. Please try again."
                    }
                    dialogUtils.showErrorDialog(message)
                }
            }
        }
    }

    private fun setupRecyclerView(vehicleClasses: List<VehicleClassData>) {
        val items = vehicleClasses
            .sortedBy { it.sortOrder }
            .map { vc ->
                VehicleClassItem(
                    classId = vc.vehicleClass,
                    className = vc.displayName,
                    weightRange = vc.weightRange,
                    vehicleTypes = vc.vehicleTypes,
                    indicatorColor = mapColorCode(vc.colorCode)
                )
            }

        adapter = VehicleClassAdapter(items) {
            binding.hasSelection = adapter?.hasSelection() ?: false
        }
        binding.rvVehicleClasses.layoutManager = LinearLayoutManager(this)
        binding.rvVehicleClasses.adapter = adapter
    }

    private fun mapColorCode(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "RED" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_blue)
        }
    }

    companion object {
        const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"
        const val EXTRA_VRN = "extra_vrn"
    }
}

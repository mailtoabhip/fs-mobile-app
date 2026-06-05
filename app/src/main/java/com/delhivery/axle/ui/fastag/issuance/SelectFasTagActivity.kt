package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.request.CreateOrderRequest
import com.delhivery.axle.api.response.VehicleClassData
import com.delhivery.axle.databinding.ActivitySelectFastagBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.issuance.PaymentBreakupActivity.Companion.EXTRA_ITEMS

class SelectFasTagActivity : BaseActivity<ActivitySelectFastagBinding, SelectFasTagViewModel>() {

    override fun getViewModelClass() = SelectFasTagViewModel::class.java
    override fun layoutId() = R.layout.activity_select_fastag
    override fun requireConnection() = true

    private var adapter: VehicleClassAdapter? = null
    private var currentOrderId = ""

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
                customerMobile = "",
                vehicles = listOf(
                    com.delhivery.axle.api.request.OrderVehicleItem(
                        vrn = vrn,
                        vehicleClass = selectedItem.classId,
                        unitPrice = "100.00"
                    )
                ),
                totalAmount = "100.00",
                idempotencyKey = "ORD-${java.util.UUID.randomUUID()}"
            )
            viewModel.createOrder(request)
        }
    }

    private fun observeCreateOrderAndKyc() {
        viewModel.createOrderState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loading handled by BaseActivity
                }
                is Resource.Success -> {
                    val orderData = resource.data
                    currentOrderId = orderData?.orderId ?: ""
                    viewModel.kycOnboardValidate("IDFC")
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
                    // Loading handled by BaseActivity
                }
                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    val salesCode = intent.getStringExtra(AddVehicleActivity.EXTRA_SALES_CODE) ?: ""
                    if (data.fastagCustomerExists) {
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
                            putExtra(PaymentBreakupActivity.EXTRA_ORDER_ID, currentOrderId)
                        }
                        startActivity(navIntent)
                    } else {
                        val items = arrayListOf(
                            com.delhivery.axle.api.request.PaymentBreakupItem(
                                vehicleClass = adapter?.getSelectedItem()?.classId ?: "",
                                quantity = 1
                            )
                        )
                        val navIntent = Intent(this, FastagKycActivity::class.java).apply {
                            putExtra(FastagKycActivity.EXTRA_SALES_CODE, salesCode)
                            putExtra(FastagKycActivity.EXTRA_ORDER_ID, currentOrderId)
                            putExtra(EXTRA_ITEMS, items)
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
    }

    private fun observeViewModel() {
        viewModel.vehicleClassesState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loading handled by BaseActivity
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

        // Pre-select vehicle class if passed from vehicle verify API
        val preSelectedClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS)
        if (!preSelectedClass.isNullOrEmpty()) {
            adapter?.selectByClassId(preSelectedClass)
            binding.hasSelection = true
        }
    }

    private fun mapColorCode(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "ORANGE" -> getColor(R.color.class_red)
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
        const val EXTRA_SALES_CODE = "extra_sales_code"
    }
}

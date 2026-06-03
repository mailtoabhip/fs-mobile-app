package com.delhivery.axle.ui.fastag.tagAssignment.assign

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.OrderItem
import com.delhivery.axle.databinding.ActivityVehicleDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class VehicleDetailsActivity : BaseActivity<ActivityVehicleDetailsBinding, VehicleDetailsViewModel>() {

    override fun getViewModelClass() = VehicleDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_vehicle_details
    override fun requireConnection() = true

    private var fastagVehicleNumber: String = ""
    private var chassisNumber: String = ""

    companion object {
        private const val VEHICLE_NUMBER = "vehicle_number"
        private const val CHASSIS_NUMBER = "chassis_number"
        private const val ORDER_ID = "order_id"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            chassisNumber: String,
            orderId: String
        ): Intent {
            return Intent(context, VehicleDetailsActivity::class.java).apply {
                putExtra(VEHICLE_NUMBER, vehicleNumber)
                putExtra(CHASSIS_NUMBER, chassisNumber)
                putExtra(ORDER_ID, orderId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fastagVehicleNumber = intent.getStringExtra(VEHICLE_NUMBER) ?: ""
        chassisNumber = intent.getStringExtra(CHASSIS_NUMBER) ?: ""
        val orderId = intent.getStringExtra(ORDER_ID) ?: ""

        setupToolbar()
        setupButton()
        setupObservers()

        // Fetch order items from API
        if (orderId.isNotEmpty()) {
            viewModel.fetchOrderItems(orderId)
        } else {
            // Fallback: show vehicle number from intent
            populateVehicleDetails(fastagVehicleNumber, "True", "Exempted", "VC4")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
            WindowInsetsUtils.applyBottomSystemWindowInsets(binding.btnContinue)
        }
    }

    private fun setupToolbar() {
        binding.tvToolbarTitle.text = fastagVehicleNumber
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupButton() {
        binding.ivEditVehicle.setOnClickListener {
            // TODO: Edit vehicle number
        }

        binding.btnContinue.setOnClickListener {
            startActivity(
                FastagAssignmentActivity.newIntent(
                    context = this,
                    vehicleNumber = fastagVehicleNumber,
                    chassisNumber = chassisNumber
                )
            )
        }
    }

    private fun setupObservers() {
        viewModel.orderItemsState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    uiUtils.showProgress()
                }
                is Resource.Success -> {
                    uiUtils.hideProgress()
                    val orderItems = resource.data?.orderItems
                    if (!orderItems.isNullOrEmpty()) {
                        // Find the item matching this vehicle number, or use first item
                        val item = orderItems.find { it.vrn == fastagVehicleNumber } ?: orderItems[0]
                        populateFromOrderItem(item)
                    } else {
                        populateVehicleDetails(fastagVehicleNumber, "True", "Exempted", "VC4")
                    }
                }
                is Resource.Failure -> {
                    uiUtils.hideProgress()
                    if (resource.isNetworkError) {
                        uiUtils.showSnackbar("Network error. Please check your connection.")
                    } else {
                        uiUtils.showSnackbar(resource.errorMessage ?: "Failed to fetch vehicle details")
                    }
                    // Fallback to intent data
                    populateVehicleDetails(fastagVehicleNumber, "True", "Exempted", "VC4")
                }
            }
        })
    }

    private fun populateFromOrderItem(item: OrderItem) {
        fastagVehicleNumber = item.vrn ?: fastagVehicleNumber
        val commercialVehicle = if (item.commercialVehicle == true) "True" else "False"
        val exemptedStatus = item.exemptedStatus ?: "NA"
        val vehicleClass = item.vehicleClass ?: "VC4"

        binding.tvToolbarTitle.text = fastagVehicleNumber
        populateVehicleDetails(fastagVehicleNumber, commercialVehicle, exemptedStatus, vehicleClass)
    }

    private fun populateVehicleDetails(
        vrn: String,
        commercialVehicle: String,
        exemptedStatus: String,
        vehicleClass: String
    ) {
        binding.tvVehicleNumber.text = vrn
        binding.tvCommercialVehicle.text = commercialVehicle
        binding.tvExemptedStatus.text = exemptedStatus
        binding.tvVehicleClass.text = vehicleClass
    }
}

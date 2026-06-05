package com.delhivery.axle.ui.fastag.tagAssignment.assign

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.OrderItem
import com.delhivery.axle.databinding.ActivityVehicleDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class VehicleDetailsActivity : BaseActivity<ActivityVehicleDetailsBinding, VehicleDetailsViewModel>() {

    override fun getViewModelClass() = VehicleDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_vehicle_details
    override fun requireConnection() = true

    private var fastagVehicleNumber: String = ""
    private var chassisNumber: String = ""
    private var orderId: String = ""
    private var orderItemId: Int = 0
    private var tagColor: String = ""
    private var bank: String = ""
    private var vehicleClass: String = ""

    companion object {
        private const val VEHICLE_NUMBER = "extra_vehicle_class"
        private const val CHASSIS_NUMBER = "extra_chassis_number"
        private const val ORDER_ID = "extra_order_id"

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
        val orderId = intent.getStringExtra(ORDER_ID) ?: ""

        setupToolbar()
        setupButton()
        setupObservers()

        // Hide content and button until API responds
        binding.scrollContent.visibility = View.GONE
        binding.btnContinue.visibility = View.GONE

        if (orderId.isNotEmpty()) {
            viewModel.fetchOrderItems(orderId)
        } else {
            dialogUtils.showErrorDialog("Order ID not available. Please try again.", 3L)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentLl)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = fastagVehicleNumber
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { navigateToHome() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    private fun navigateToHome() {
        val homeIntent = Intent(this, HomeActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finish()
    }

    private fun setupButton() {

        binding.btnContinue.setOnClickListener {
            startActivity(
                RCUploadActivity.newIntent(
                    context = this,
                    vehicleNumber = fastagVehicleNumber,
                    orderId = orderId,
                    orderItemId = orderItemId,
                    tagColor = tagColor,
                    bank = bank,
                    vehicleClass = vehicleClass
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
                    val orderItems = resource.data
                    if (!orderItems.isNullOrEmpty()) {
                        val item = orderItems.find { it.vrn == fastagVehicleNumber } ?: orderItems[0]
                        populateFromOrderItem(item)
                        // Show content after successful load
                        binding.scrollContent.visibility = View.VISIBLE
                        binding.btnContinue.visibility = View.VISIBLE
                    } else {
                        dialogUtils.showErrorDialog("No vehicle details found for this order.", 3L)
                    }
                }
                is Resource.Failure -> {
                    uiUtils.hideProgress()
                    val errorMessage = if (resource.isNetworkError) {
                        "Network error. Please check your connection and try again."
                    } else {
                        resource.errorMessage ?: "Failed to fetch vehicle details. Please try again."
                    }
                    dialogUtils.showErrorDialog(errorMessage, 3L)
                }
            }
        })
    }

    private fun populateFromOrderItem(item: OrderItem) {
        fastagVehicleNumber = item.vrn ?: fastagVehicleNumber
        orderId = item.orderId ?: intent.getStringExtra(ORDER_ID) ?: ""
        orderItemId = item.orderItemId ?: 0
        tagColor = item.tagColor ?: ""
        bank = item.bank ?: ""
        vehicleClass = item.vehicleClass ?: ""

        val commercialVehicle = when (item.isCommercial) {
            true -> "True"
            false -> "False"
            null -> "NA"
        }
        val exemptedState = item.exemptedState ?: "NA"
        val vehicleClass = item.vehicleClass ?: ""

        binding.toolbar.title = fastagVehicleNumber
        binding.tvVehicleNumber.text = fastagVehicleNumber
        binding.tvCommercialVehicle.text = commercialVehicle
        binding.tvExemptedStatus.text = exemptedState
        binding.tvVehicleClass.text = vehicleClass
    }
}

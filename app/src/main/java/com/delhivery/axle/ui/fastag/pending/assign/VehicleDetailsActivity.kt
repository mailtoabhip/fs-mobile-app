package com.delhivery.axle.ui.fastag.pending.assign

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVehicleDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class VehicleDetailsActivity : BaseActivity<ActivityVehicleDetailsBinding, VehicleDetailsViewModel>() {

    override fun getViewModelClass() = VehicleDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_vehicle_details
    override fun requireConnection() = true

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"
        private const val EXTRA_CHASSIS_NUMBER = "extra_chassis_number"
        private const val EXTRA_COMMERCIAL_VEHICLE = "extra_commercial_vehicle"
        private const val EXTRA_EXEMPTED_STATUS = "extra_exempted_status"
        private const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            chassisNumber: String,
            commercialVehicle: String = "True",
            exemptedStatus: String = "Exempted",
            vehicleClass: String = "VC4"
        ): Intent {
            return Intent(context, VehicleDetailsActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
                putExtra(EXTRA_CHASSIS_NUMBER, chassisNumber)
                putExtra(EXTRA_COMMERCIAL_VEHICLE, commercialVehicle)
                putExtra(EXTRA_EXEMPTED_STATUS, exemptedStatus)
                putExtra(EXTRA_VEHICLE_CLASS, vehicleClass)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val chassisNumber = intent.getStringExtra(EXTRA_CHASSIS_NUMBER) ?: ""
        val commercialVehicle = intent.getStringExtra(EXTRA_COMMERCIAL_VEHICLE) ?: "True"
        val exemptedStatus = intent.getStringExtra(EXTRA_EXEMPTED_STATUS) ?: "Exempted"
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: "VC4"

        setupToolbar(vehicleNumber)
        setupVehicleDetails(vehicleNumber, commercialVehicle, exemptedStatus, vehicleClass)
        setupButton(vehicleNumber, chassisNumber)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
            WindowInsetsUtils.applyBottomSystemWindowInsets(binding.btnContinue)
        }
    }

    private fun setupToolbar(vehicleNumber: String) {
        binding.tvToolbarTitle.text = vehicleNumber
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupVehicleDetails(
        vehicleNumber: String,
        commercialVehicle: String,
        exemptedStatus: String,
        vehicleClass: String
    ) {
        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvCommercialVehicle.text = commercialVehicle
        binding.tvExemptedStatus.text = exemptedStatus
        binding.tvVehicleClass.text = vehicleClass
    }

    private fun setupButton(vehicleNumber: String, chassisNumber: String) {
        binding.ivEditVehicle.setOnClickListener {
            // TODO: Edit vehicle number
        }

        binding.btnContinue.setOnClickListener {
            startActivity(
                FastagAssignmentActivity.newIntent(
                    context = this,
                    vehicleNumber = vehicleNumber,
                    chassisNumber = chassisNumber
                )
            )
        }
    }
}

package com.delhivery.axle.ui.fastag.pending.assign

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAssignVehicleBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.pending.PendingActionType
import com.delhivery.axle.utils.WindowInsetsUtils

class AssignVehicleActivity : BaseActivity<ActivityAssignVehicleBinding, AssignVehicleViewModel>() {

    override fun getViewModelClass() = AssignVehicleViewModel::class.java
    override fun layoutId() = R.layout.activity_assign_vehicle
    override fun requireConnection() = true

    private lateinit var adapter: SelectVehicleAdapter

    companion object {
        private const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"
        private const val EXTRA_REFERENCE_ID = "extra_reference_id"
        private const val EXTRA_COLOR_CODE = "extra_color_code"
        private const val EXTRA_ACTION_TYPE = "extra_action_type"

        fun newIntent(
            context: Context,
            vehicleClass: String,
            referenceId: String?,
            colorCode: String,
            actionType: PendingActionType
        ): Intent {
            return Intent(context, AssignVehicleActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_CLASS, vehicleClass)
                putExtra(EXTRA_REFERENCE_ID, referenceId)
                putExtra(EXTRA_COLOR_CODE, colorCode)
                putExtra(EXTRA_ACTION_TYPE, actionType.name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        val referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID)
        val colorCode = intent.getStringExtra(EXTRA_COLOR_CODE) ?: "GREEN"
        val actionType = try {
            PendingActionType.valueOf(intent.getStringExtra(EXTRA_ACTION_TYPE) ?: "ASSIGNMENT")
        } catch (e: Exception) {
            PendingActionType.ASSIGNMENT
        }

        setupToolbar(actionType)
        setupVehicleHeader(vehicleClass, referenceId, colorCode)
        setupRecyclerView()
        setupObservers()

        viewModel.fetchAvailableVehicles()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentLl)
        }
    }

    private fun setupToolbar(actionType: PendingActionType) {
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupVehicleHeader(vehicleClass: String, referenceId: String?, colorCode: String) {
        binding.tvVehicleClass.text = vehicleClass
        if (referenceId != null) {
            binding.tvReferenceId.text = referenceId
            binding.tvReferenceId.visibility = View.VISIBLE
        } else {
            binding.tvReferenceId.visibility = View.GONE
        }

        // Set tag icon color based on colorCode
        val tagColor = mapColor(colorCode)
        binding.ivTagIcon.setColorFilter(tagColor)
    }

    private fun setupRecyclerView() {
        adapter = SelectVehicleAdapter { vehicle ->
            startActivity(
                VehicleDetailsActivity.newIntent(
                    context = this,
                    vehicleNumber = vehicle.vehicleNumber,
                    chassisNumber = vehicle.chassisNumber
                )
            )
        }
        binding.rvVehicles.apply {
            layoutManager = LinearLayoutManager(this@AssignVehicleActivity)
            adapter = this@AssignVehicleActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this, Observer { loading ->
            binding.shimmerLayout.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.rvVehicles.visibility = View.GONE
            }
        })

        viewModel.availableVehicles.observe(this, Observer { vehicles ->
            if (vehicles.isNullOrEmpty()) {
                binding.rvVehicles.visibility = View.GONE
                binding.tvSelectVehicle.visibility = View.GONE
            } else {
                binding.rvVehicles.visibility = View.VISIBLE
                binding.tvSelectVehicle.visibility = View.VISIBLE
                adapter.submitList(vehicles)
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            errorMsg?.let { uiUtils.showSnackbar(it) }
        })
    }

    private fun mapColor(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "RED" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_green)
        }
    }
}

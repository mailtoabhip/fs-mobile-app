package com.delhivery.axle.ui.fastag.tagAssignment.pendingActions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAssignVehicleBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.tagAssignment.assign.SelectVehicleAdapter
import com.delhivery.axle.ui.fastag.tagAssignment.assign.VehicleDetailsActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class AssignVehicleActivity : BaseActivity<ActivityAssignVehicleBinding, AssignVehicleViewModel>() {

    override fun getViewModelClass() = AssignVehicleViewModel::class.java
    override fun layoutId() = R.layout.activity_assign_vehicle
    override fun requireConnection() = true

    private lateinit var adapter: SelectVehicleAdapter

    companion object {
        private const val VEHICLE_CLASS = "vehicle_class"
        private const val REFERENCE_ID = "reference_id"
        private const val COLOR_CODE = "color_code"
        private const val ACTION_TYPE = "action_type"

        fun newIntent(
            context: Context,
            vehicleClass: String,
            referenceId: String?,
            colorCode: String,
            actionType: PendingActionType
        ): Intent {
            return Intent(context, AssignVehicleActivity::class.java).apply {
                putExtra(VEHICLE_CLASS, vehicleClass)
                putExtra(REFERENCE_ID, referenceId)
                putExtra(COLOR_CODE, colorCode)
                putExtra(ACTION_TYPE, actionType.name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleClass = intent.getStringExtra(VEHICLE_CLASS) ?: ""
        val referenceId = intent.getStringExtra(REFERENCE_ID)
        val colorCode = intent.getStringExtra(COLOR_CODE) ?: "GREEN"
        val actionType = try {
            PendingActionType.valueOf(intent.getStringExtra(ACTION_TYPE) ?: "ASSIGNMENT")
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
        val referenceId = intent.getStringExtra(REFERENCE_ID) ?: ""
        adapter = SelectVehicleAdapter { vehicle ->
            startActivity(
                VehicleDetailsActivity.Companion.newIntent(
                    context = this,
                    vehicleNumber = vehicle.vehicleNumber,
                    chassisNumber = vehicle.chassisNumber,
                    orderId = referenceId
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

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
import com.delhivery.axle.ui.fastag.issuance.AddVehicleActivity
import com.delhivery.axle.ui.fastag.issuance.FastagCollectionActivity
import com.delhivery.axle.ui.fastag.issuance.FastagKycActivity
import com.delhivery.axle.ui.fastag.issuance.PaymentBreakupActivity
import com.delhivery.axle.ui.fastag.issuance.SelectFasTagActivity
import com.delhivery.axle.ui.fastag.tagAssignment.assign.kyv.KYVFastagImageUploadActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class AssignVehicleActivity : BaseActivity<ActivityAssignVehicleBinding, AssignVehicleViewModel>() {

    override fun getViewModelClass() = AssignVehicleViewModel::class.java
    override fun layoutId() = R.layout.activity_assign_vehicle
    override fun requireConnection() = true

    private lateinit var adapter: SelectVehicleAdapter
    private var actionType: PendingActionType = PendingActionType.ORDER_CREATED

    companion object {
        private const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"
        private const val EXTRA_REFERENCE_ID = "extra_reference_id"
        private const val EXTRA_COLOR_CODE = "extra_color_code"
        private const val EXTRA_ACTION_TYPE = "extra_action_type"
        private const val EXTRA_VRN = "extra_vrn"
        private const val EXTRA_BARCODE_ID = "extra_barcode_id"
        private const val EXTRA_SALES_CODE = "extra_sales_code"
        private const val EXTRA_ORDER_ID = "extra_order_id"
        private const val EXTRA_ITEM_ID = "extra_item_id"
        private const val EXTRA_JOURNEY_ID = "extra_journey_id"
        private const val EXTRA_ITEMS = "extra_items"

        fun newIntent(
            context: Context,
            vehicleClass: String,
            referenceId: String?,
            colorCode: String,
            actionType: PendingActionType,
            vrn: String? = null,
            barcodeId: String? = null,
            salesCode: String? = null,
            orderId: String? = null,
            itemId: String? = null,
            journeyId: String? = null,
            items: ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem>? = null
        ): Intent {
            return Intent(context, AssignVehicleActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_CLASS, vehicleClass)
                putExtra(EXTRA_REFERENCE_ID, referenceId)
                putExtra(EXTRA_COLOR_CODE, colorCode)
                putExtra(EXTRA_ACTION_TYPE, actionType.name)
                putExtra(EXTRA_VRN, vrn)
                putExtra(EXTRA_BARCODE_ID, barcodeId)
                putExtra(EXTRA_SALES_CODE, salesCode)
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_JOURNEY_ID, journeyId)
                putExtra(EXTRA_ITEMS, items)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        val referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID)
        val colorCode = intent.getStringExtra(EXTRA_COLOR_CODE) ?: "GREEN"
        val vrn = intent.getStringExtra(EXTRA_VRN)
        val barcodeId = intent.getStringExtra(EXTRA_BARCODE_ID)
        val actionType = try {
            PendingActionType.valueOf(intent.getStringExtra(EXTRA_ACTION_TYPE) ?: "ASSIGNMENT")
        } catch (e: Exception) {
            PendingActionType.ORDER_CREATED
        }
        this.actionType = actionType

        setupToolbar(actionType)
        setupVehicleHeader(vehicleClass, barcodeId, colorCode)
        setupRecyclerView()
        setupObservers()

        viewModel.fetchAvailableVehicles(vrn)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentLl)
        }
    }

    private fun setupToolbar(actionType: PendingActionType) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
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
        val referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID) ?: ""
        adapter = SelectVehicleAdapter { vehicle ->
            handleVehicleSelection(vehicle)
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
            "ORANGE" -> getColor(R.color.vehicle_class_orange)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_green)
        }
    }

    /**
     * Handles navigation based on the current action type after vehicle selection.
     */
    private fun handleVehicleSelection(vehicle: AvailableVehicle) {
        val salesCode = intent.getStringExtra(EXTRA_SALES_CODE) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: ""
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        val referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID)
        val barcodeId = intent.getStringExtra(EXTRA_BARCODE_ID)

        when (actionType) {
            PendingActionType.ADD_VEHICLE -> {
                val intent = Intent(this, AddVehicleActivity::class.java).apply {
                    putExtra(AddVehicleActivity.EXTRA_TRUCK_NUMBER, vehicle.vehicleNumber)
                    putExtra(AddVehicleActivity.EXTRA_SALES_CODE, salesCode)
                }
                startActivity(intent)
            }
            PendingActionType.ORDER_CREATED -> {
                val intent = Intent(this, SelectFasTagActivity::class.java).apply {
                    putExtra(SelectFasTagActivity.EXTRA_SALES_CODE, salesCode)
                    putExtra(SelectFasTagActivity.EXTRA_VRN, vehicleNumber)
                }
                startActivity(intent)
            }
            PendingActionType.KYC_DONE -> {
                @Suppress("DEPRECATION", "UNCHECKED_CAST")
                val items = intent.getSerializableExtra(EXTRA_ITEMS) as? ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem>
                val intent = Intent(this, FastagKycActivity::class.java).apply {
                    putExtra(FastagKycActivity.EXTRA_SALES_CODE, salesCode)
                    putExtra(FastagKycActivity.EXTRA_ORDER_ID, orderId)
                    putExtra(PaymentBreakupActivity.EXTRA_ITEMS, items)
                }
                startActivity(intent)
            }
            PendingActionType.FULL_PAYMENT_PARTIAL_PAYMENT -> {
                @Suppress("DEPRECATION", "UNCHECKED_CAST")
                val items = intent.getSerializableExtra(EXTRA_ITEMS) as? ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem>
                    ?: arrayListOf()
                val paymentIntent = Intent(this, PaymentBreakupActivity::class.java).apply {
                    putExtra(PaymentBreakupActivity.EXTRA_SALES_CODE, salesCode)
                    putExtra(PaymentBreakupActivity.EXTRA_PAYMENT_METHOD, "FULL_PAYMENT")
                    putExtra(PaymentBreakupActivity.EXTRA_BANK_PARTNER_CODE, "IDFC")
                    putExtra(PaymentBreakupActivity.EXTRA_VEHICLE_CLASS_QTY, "1")
                    putExtra(PaymentBreakupActivity.EXTRA_ORDER_ID, orderId)
                    putExtra(PaymentBreakupActivity.EXTRA_ITEMS, items)
                }
                android.util.Log.d("AssignVehicle", "Payment items passed: ${items.size} → $items")
                startActivity(paymentIntent)
            }
            PendingActionType.HOTO_DONE -> {
                val intent = Intent(this, FastagCollectionActivity::class.java).apply {
                    putExtra(FastagCollectionActivity.EXTRA_SALES_CODE, salesCode)
                    putExtra(FastagCollectionActivity.EXTRA_ORDER_ID, orderId)
                }
                startActivity(intent)
            }
            PendingActionType.TAG_ASSIGNMENT -> {
                val intent = VehicleDetailsActivity.newIntent(
                    this,
                    vehicle.vehicleNumber,
                    "",
                    orderId
                )
                startActivity(intent)
            }
            PendingActionType.KYV -> {
                val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
                startActivity(KYVFastagImageUploadActivity.newIntent(this, vehicle.vehicleNumber, journeyId = journeyId, orderId=orderId, itemId = itemId))
            }
        }
    }
}

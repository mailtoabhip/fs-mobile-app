package com.delhivery.axle.ui.fastag

import android.os.Bundle
import android.widget.Toast
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagTransactionDetailBinding
import com.delhivery.axle.ui.base.BaseActivity

class FastagTransactionDetailActivity : BaseActivity<ActivityFastagTransactionDetailBinding, FastagTransactionDetailsViewModel>() {

    override fun getViewModelClass() = FastagTransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_transaction_detail
    override fun requireConnection() = false

    companion object {
        const val EXTRA_TXN_ID = "txn_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_TOLL_NAME = "toll_name"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_VEHICLE_NUMBER = "vehicle_number"
        const val EXTRA_TRUCK_SIZE = "truck_size"
        const val EXTRA_CAPACITY = "capacity"
        const val EXTRA_OWNERSHIP = "ownership"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle window insets for edge-to-edge display (API 35+)
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }

        setupUI()
    }

    private fun setupUI() {
        // Get data from intent - using hardcoded values for now
        val txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: "TXN-4925892SM7"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 310.0)
        val tollName = intent.getStringExtra(EXTRA_TOLL_NAME) ?: "Gurgaon Toll Plaza"
        val timestamp = intent.getStringExtra(EXTRA_TIMESTAMP) ?: "8th Dec 2023, 02:54 PM"
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: "RJ-14-GH-1234"
        val truckSize = intent.getStringExtra(EXTRA_TRUCK_SIZE) ?: "32FTXXL"
        val capacity = intent.getDoubleExtra(EXTRA_CAPACITY, 18.0)
        val ownership = intent.getStringExtra(EXTRA_OWNERSHIP) ?: "Own Truck"

        // Set vehicle info
        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvVehicleMeta.text = "$ownership | $truckSize | $capacity MT"

        // Set FASTag info
        binding.tvFastagProvider.text = "IDFC FASTag by Delhivery"
        binding.tvFastagId.text = "FASTag ID: FT-9928341029"

        // Set amount
        val amountText = if (amount < 0) "-₹${kotlin.math.abs(amount).toInt()}" else "+₹${amount.toInt()}"
        binding.tvAmount.text = amountText

        // Set transaction details
        binding.tvTollName.text = tollName
        binding.tvDateTime.text = timestamp
        binding.tvTransactionId.text = txnId

        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Raise Dispute button
        binding.btnRaiseDispute.setOnClickListener {
            Toast.makeText(this, "Raise Dispute functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}

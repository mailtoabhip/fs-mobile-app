package com.delhivery.axle.ui.test

import android.os.Bundle
import android.widget.Toast
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DriverDataResponse
import com.delhivery.axle.databinding.ActivityDriverAutocompleteTestBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.custom.DelhiveryDriverNameAutoEditText
import com.delhivery.axle.utils.AutoCompleteUtils
import javax.inject.Inject

/**
 * Test activity for driver autocomplete functionality
 * This activity demonstrates how to use the autocomplete with mock data
 */
class DriverAutocompleteTestActivity : BaseActivity<ActivityDriverAutocompleteTestBinding, DriverAutocompleteTestViewModel>() {

    @Inject
    lateinit var autoCompleteUtils: AutoCompleteUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable test mode to use mock data
        autoCompleteUtils.enableTestMode()
        
        setupDriverAutocomplete()
        setupVehicleNumberInput()
    }

    private fun setupDriverAutocomplete() {
        // Setup driver name autocomplete
        autoCompleteUtils.autoCompleteDriverNameWithPhone(
            binding.editDriverName,
            { binding.editVehicleNumber.text.toString() } // Vehicle number provider
        ) { driverData ->
            handleDriverSelection(driverData)
        }
    }

    private fun setupVehicleNumberInput() {
        // Add text change listener to vehicle number to trigger driver loading
        binding.editVehicleNumber.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val vehicleNumber = s.toString()
                if (vehicleNumber.length >= 2) {
                    // Clear driver name when vehicle number changes
                    binding.editDriverName.setText("")
                    binding.textDriverInfo.text = "Enter driver name to see suggestions..."
                }
            }
        })
    }

    private fun handleDriverSelection(driverData: DriverDataResponse) {
        val driverName = driverData.driverName ?: ""
        val driverPhone = driverData.driverPhone ?: ""
        
        if (driverName.isNotEmpty()) {
            binding.textDriverInfo.text = "Selected Driver:\nName: $driverName\nPhone: $driverPhone"
            
            // Show toast for demonstration
            Toast.makeText(this, "Driver selected: $driverName", Toast.LENGTH_SHORT).show()
        } else {
            binding.textDriverInfo.text = "Enter driver name to see suggestions..."
        }
    }

    fun onTestVehicleClick(view: android.view.View) {
        binding.editVehicleNumber.setText("MP09QT0001")
        binding.editDriverName.setText("")
        binding.textDriverInfo.text = "Vehicle set to MP09QT0001. Now type a driver name..."
    }

    fun onTestVehicle2Click(view: android.view.View) {
        binding.editVehicleNumber.setText("MP09QT0002")
        binding.editDriverName.setText("")
        binding.textDriverInfo.text = "Vehicle set to MP09QT0002. Now type a driver name..."
    }

    override fun getViewModelClass() = DriverAutocompleteTestViewModel::class.java
    override fun layoutId() = R.layout.activity_driver_autocomplete_test
    override fun requireConnection() = false
}

// Simple view model for the test activity
class DriverAutocompleteTestViewModel : com.delhivery.axle.ui.base.BaseViewModel() {
    // No specific logic needed for this test activity
}

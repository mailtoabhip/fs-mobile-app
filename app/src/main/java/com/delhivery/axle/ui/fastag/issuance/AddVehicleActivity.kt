package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.ActivityAddVehicleBinding
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class AddVehicleActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var viewModel: AddVehicleViewModel

    private var isVehicleEligible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_vehicle)
        binding.truckNumber = ""
        viewModel = ViewModelProvider(this, viewModelFactory)[AddVehicleViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupTextWatcher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Vehicle"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTextWatcher() {
        binding.etTruckNumber.addTextChangedListener { text ->
            isVehicleEligible = false
            val truckNumber = text?.toString()?.trim() ?: ""
            if (isValidVehicleNumber(truckNumber)) {
                viewModel.checkVehicle(truckNumber.uppercase().replace(" ", "").replace("-", ""))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val truckNumber = binding.truckNumber?.trim() ?: return@setOnClickListener
            if (isVehicleEligible) {
                navigateToSelectFasTag(truckNumber)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.vehicleCheckState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Optionally show a small loader on the input field
                }

                is Resource.Success -> {
                    val data = resource.data ?: return@observe
                    if (data.isEligible && !data.isHotlisted) {
                        isVehicleEligible = true
                        binding.btnContinue.isEnabled = true
                        binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)
                    } else if (data.isHotlisted) {
                        isVehicleEligible = false
                        binding.btnContinue.isEnabled = false
                        binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                        showHotlistedBottomSheet(
                            vehicleNumber = data.vehicleNumber,
                            issuerPhone = data.issuerPhone ?: ""
                        )
                    } else {
                        isVehicleEligible = false
                        binding.btnContinue.isEnabled = false
                        binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                        Toast.makeText(this, data.message ?: "Vehicle not eligible", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Failure -> {
                    isVehicleEligible = false
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.setBackgroundResource(R.drawable.bg_all_round_corner_light_grey)
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        val normalized = vehicleNumber
            .uppercase()
            .replace(" ", "")
            .replace("-", "")

        val regex = Regex(
            "^(?:[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{1,4}|\\d{2}BH\\d{4}[A-Z]{2})$"
        )
        return regex.matches(normalized)
    }

    private fun navigateToSelectFasTag(truckNumber: String) {
        val intent = Intent(this, SelectFasTagActivity::class.java).apply {
            putExtra(EXTRA_TRUCK_NUMBER, truckNumber)
        }
        startActivity(intent)
    }

    private fun showHotlistedBottomSheet(vehicleNumber: String, issuerPhone: String = "") {
        val bottomSheet = FastagHotlistedBottomSheet.newInstance(
            vehicleNumber = vehicleNumber,
            issuerPhone = issuerPhone
        )
        bottomSheet.show(supportFragmentManager, FastagHotlistedBottomSheet.TAG)
    }

    companion object {
        const val EXTRA_TRUCK_NUMBER = "extra_truck_number"
    }
}

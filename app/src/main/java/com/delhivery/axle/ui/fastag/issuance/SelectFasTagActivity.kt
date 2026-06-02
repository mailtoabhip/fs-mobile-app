package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.VehicleClassData
import com.delhivery.axle.databinding.ActivitySelectFastagBinding
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SelectFasTagActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivitySelectFastagBinding
    private lateinit var viewModel: SelectFasTagViewModel
    private var adapter: VehicleClassAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_fastag)
        binding.hasSelection = false

        viewModel = ViewModelProvider(this, viewModelFactory)[SelectFasTagViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()
        observeViewModel()

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
            val intent = Intent(this, FastagKycActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_CLASS, selectedItem.classId)
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.vehicleClassesState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show shimmer/progress if needed
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
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
    }

    private fun mapColorCode(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "RED" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_blue)
        }
    }

    companion object {
        const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"
    }
}

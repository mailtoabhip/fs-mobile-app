package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.FastagOrder
import com.delhivery.axle.api.response.FastagInventoryItem
import com.delhivery.axle.databinding.ActivityFastagCollectionBinding
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class FastagCollectionActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var dialogUtils: com.delhivery.axle.utils.DialogUtils

    private lateinit var binding: ActivityFastagCollectionBinding
    private lateinit var viewModel: FastagCollectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fastag_collection)
        viewModel = ViewModelProvider(this, viewModelFactory)[FastagCollectionViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        observeViewModel()

        binding.slideToConfirm.setLabelText("I have collected all the tags")
        binding.slideToConfirm.setOnSlideCompleteListener {
            confirmCollection()
        }

        viewModel.fetchOrders(intent.getStringExtra("extra_sales_code") ?: "")
    }

    private var currentOrderId: String = ""
    private var currentSalesCode: String = ""

    private fun confirmCollection() {
        // TODO: Build actual products list from scanned/collected data
        val request = com.delhivery.axle.api.request.ConfirmCollectionRequest(
            orderId = currentOrderId,
            salesCode = currentSalesCode,
            deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID),
            products = emptyList() // TODO: Populate with actual collected product data
        )
        viewModel.confirmCollection(currentOrderId, request)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "FASTag Collection"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun observeViewModel() {
        viewModel.ordersState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading state
                }

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null && data.orders.isNotEmpty()) {
                        loadCollectionData(data.orders.first())
                    }
                }

                is Resource.Failure -> {
                    val message = if (resource.isNetworkError) {
                        "No internet connection. Please try again."
                    } else {
                        "Something went wrong. Please try again."
                    }
                    dialogUtils.showErrorDialog(message)
                }
            }
        }

        viewModel.confirmState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // TODO: Show loading on slide button
                }

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null && (data.status == "SUCCESS" || data.status == "PARTIAL_SUCCESS")) {
                        showCollectionSuccessBottomSheet()
                    } else {
                        binding.slideToConfirm.reset()
                        Toast.makeText(this, data?.message ?: "Collection failed", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Failure -> {
                    binding.slideToConfirm.reset()
                    val message = if (resource.isNetworkError) {
                        "No internet connection. Please try again."
                    } else {
                        "Something went wrong. Please try again."
                    }
                    dialogUtils.showErrorDialog(message)
                }
            }
        }
    }

    private fun showCollectionSuccessBottomSheet() {
        CollectionSuccessBottomSheet.newInstance {
            // TODO: Navigate to Vehicle Verification screen
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }.show(supportFragmentManager, CollectionSuccessBottomSheet.TAG)
    }

    private fun loadCollectionData(order: FastagOrder) {
        currentOrderId = order.orderId
        currentSalesCode = order.salesCode
        binding.orderId = order.orderId

        val totalTags = order.items.sumOf { it.dispatchedQuantity }
        binding.totalTags = totalTags

        val inventoryItems = order.items.map { item ->
            FastagInventoryItem(
                vehicleClass = item.vehicleClass,
                displayName = item.displayName,
                vehicleTypes = item.vehicleTypes,
                units = item.dispatchedQuantity,
                colorCode = item.colorCode
            )
        }

        val adapter = FastagInventoryAdapter(inventoryItems) { colorCode ->
            mapColorCode(colorCode)
        }
        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter
    }

    private fun mapColorCode(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "RED" -> getColor(R.color.class_red)
            "ORANGE" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_blue)
        }
    }
}

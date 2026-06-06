package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.FastagOrdersResponse
import com.delhivery.axle.api.response.FastagInventoryItem
import com.delhivery.axle.databinding.ActivityFastagCollectionBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.tagAssignment.assign.VehicleDetailsActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import javax.inject.Inject

class FastagCollectionActivity : BaseActivity<ActivityFastagCollectionBinding, FastagCollectionViewModel>() {
    companion object {
        const val EXTRA_SALES_CODE = "extra_sales_code"
        const val EXTRA_ORDER_ID = "extra_order_id"
    }

    override fun getViewModelClass() = FastagCollectionViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_collection
    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupBackPressToHome()
        binding.lifecycleOwner = this

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

        viewModel.fetchOrders(intent.getStringExtra(EXTRA_SALES_CODE) ?: "", intent.getStringExtra(EXTRA_ORDER_ID) ?: "")
    }

    private var currentOrderId: String = ""
    private var currentSalesCode: String = ""

    private fun confirmCollection() {
        val request = com.delhivery.axle.api.request.ConfirmCollectionRequest(
            orderId = currentOrderId,
            salesCode = currentSalesCode
        )
        viewModel.confirmCollection(request)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "FASTag Collection"
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            navigationUtils.navigateToHome()
        }
    }

    private fun observeViewModel() {
        viewModel.ordersState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null) {
                        loadCollectionData(data)
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
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null && (data.status == "SUCCESS" || data.status == "PARTIAL_SUCCESS")) {
                        showCollectionSuccessBottomSheet()
                    } else {
                        binding.slideToConfirm.reset()
                        showCollectionFailedBottomSheet()
                    }
                }

                is Resource.Failure -> {
                    binding.slideToConfirm.reset()
                    showCollectionFailedBottomSheet()
                }
            }
        }
    }

    private fun showCollectionSuccessBottomSheet() {
        CollectionSuccessBottomSheet.newInstance {
            // Assign FASTags — navigate to vehicle details
            val intent = Intent(this, VehicleDetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_ORDER_ID, currentOrderId)
            }
            startActivity(intent)
            finish()
        }.show(supportFragmentManager, CollectionSuccessBottomSheet.TAG)
    }

    private fun showCollectionFailedBottomSheet() {
        CollectionSuccessBottomSheet.newFailedInstance {
            // Try again — reset slide and retry
            binding.slideToConfirm.reset()
            confirmCollection()
        }.show(supportFragmentManager, CollectionSuccessBottomSheet.TAG)
    }

    private fun loadCollectionData(order: FastagOrdersResponse) {
        currentOrderId = order.orderId
        currentSalesCode = order.salesCode
        binding.orderId = order.orderId

        val totalTags = order.items.size
        binding.totalTags = totalTags

        // Use vehicle_class_summary if available, otherwise group from items
        val inventoryItems = if (!order.vehicleClassSummary.isNullOrEmpty()) {
            order.vehicleClassSummary.map { summary ->
                FastagInventoryItem(
                    vehicleClass = summary.vehicleClass,
                    displayName = summary.displayName,
                    vehicleTypes = summary.vehicleTypes,
                    units = summary.count,
                    colorCode = summary.colorCode
                )
            }
        } else {
            order.items
                .groupBy { it.vehicleClass }
                .map { (_, items) ->
                    val first = items.first()
                    FastagInventoryItem(
                        vehicleClass = first.vehicleClass,
                        displayName = first.displayName,
                        vehicleTypes = first.vehicleTypes,
                        units = items.size,
                        colorCode = first.colorCode
                    )
                }
        }

        val adapter = FastagInventoryAdapter(inventoryItems) { colorCode ->
            mapColorCode(colorCode)
        }
        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter
    }

    private fun mapColorCode(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "ORANGE" -> getColor(R.color.class_red)
            "YELLOW" -> getColor(R.color.class_yellow)
            "GREEN" -> getColor(R.color.class_green)
            "PINK" -> getColor(R.color.class_pink)
            "BLUE" -> getColor(R.color.class_blue)
            else -> getColor(R.color.class_blue)
        }
    }
}

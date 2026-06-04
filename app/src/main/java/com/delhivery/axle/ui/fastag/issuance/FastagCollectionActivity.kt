package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.FastagOrder
import com.delhivery.axle.api.response.FastagInventoryItem
import com.delhivery.axle.databinding.ActivityFastagCollectionBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity

class FastagCollectionActivity : BaseActivity<ActivityFastagCollectionBinding, FastagCollectionViewModel>() {
    companion object {
        const val EXTRA_SALES_CODE = "extra_sales_code"
        const val EXTRA_FASTAG_ID = "extra_fastag_id"
        const val EXTRA_BARCODE = "extra_barcode"
        const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"
        const val EXTRA_VRN = "extra_vrn"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getViewModelClass() = FastagCollectionViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_collection
    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        viewModel.fetchOrders(intent.getStringExtra("extra_sales_code") ?: "")
    }

    private var currentOrderId: String = ""
    private var currentSalesCode: String = ""

    private fun confirmCollection() {
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
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.ordersState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}

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
                is Resource.Loading -> {}

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

        val totalTags = order.items.size
        binding.totalTags = totalTags

        val inventoryItems = order.items
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

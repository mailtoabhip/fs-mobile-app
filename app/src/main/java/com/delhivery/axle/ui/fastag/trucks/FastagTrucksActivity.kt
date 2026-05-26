package com.delhivery.axle.ui.fastag.trucks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagTrucksBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.BuyFastagBottomSheetDialogFragment
import com.delhivery.axle.ui.fastag.fastag_details.FastagTransactionDetailsActivity
import com.delhivery.axle.ui.fastag.recharge.FastagRechargeActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class FastagTrucksActivity : BaseActivity<ActivityFastagTrucksBinding, FastagTrucksViewModel>() {

    override fun getViewModelClass() = FastagTrucksViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_trucks
    override fun requireConnection() = true

    private lateinit var adapter: FastagTruckAdapter

    private val searchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedVehicle = result.data?.getStringExtra(FastagSearchActivity.RESULT_VEHICLE_NUMBER)
            selectedVehicle?.let { vehicleNumber ->
                val truck = adapter.currentList.firstOrNull {
                    it.vehicleNumber.equals(vehicleNumber, ignoreCase = true)
                }
                truck?.let {
                    val intent = Intent(this, FastagTransactionDetailsActivity::class.java).apply {
                        putExtra(FastagTransactionDetailsActivity.VEHICLE_DATA, it)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, FastagTrucksActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupBanner()
        setupObservers()

        viewModel.fetchFastagTrucks()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh on return from recharge
        viewModel.fetchFastagTrucks()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivSearch.setOnClickListener {
            val vehicleNumbers = ArrayList(
                adapter.currentList.map { it.vehicleNumber }
            )
            searchLauncher.launch(FastagSearchActivity.newIntent(this, vehicleNumbers))
        }
    }

    private fun setupRecyclerView() {
        adapter = FastagTruckAdapter(
            onRechargeClick = { truck ->
                val intent = Intent(this, FastagRechargeActivity::class.java).apply {
                    putExtra(FastagRechargeActivity.TAG_ID, truck.fastagTagId)
                    putExtra(FastagRechargeActivity.VEHICLE_NUMBER, truck.vehicleNumber)
                    putExtra(FastagRechargeActivity.FASTAG_BALANCE, truck.fastagBalance ?: "0")
                }
                startActivity(intent)
            },
            onRefreshClick = { truck ->
                truck.fastagTagId?.let { tagId ->
                    viewModel.refreshBalance(tagId)
                }
            },
            onItemClick = { truck ->
                val intent = Intent(this, FastagTransactionDetailsActivity::class.java).apply {
                    putExtra(FastagTransactionDetailsActivity.VEHICLE_DATA, truck)
                }
                startActivity(intent)
            }
        )

        binding.rvFastagTrucks.apply {
            layoutManager = LinearLayoutManager(this@FastagTrucksActivity)
            adapter = this@FastagTrucksActivity.adapter
        }
    }

    private fun setupBanner() {
        binding.btnBuyFastag.setOnClickListener {
            val dialog =
                BuyFastagBottomSheetDialogFragment.newInstance { selectedCity, truckCount ->
                    // Handle buy FASTag submission
                    uiUtils.showSnackbar("FASTag request submitted")
                }
            dialog.show(supportFragmentManager, "BuyFastagBottomSheet")
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this, Observer { loading ->
            binding.swipeRefresh.isRefreshing = false
            binding.shimmerLayout.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.rvFastagTrucks.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
            }
        })

        viewModel.fastagTrucks.observe(this, Observer { trucks ->
            if (trucks.isNullOrEmpty()) {
                binding.rvFastagTrucks.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvFastagTrucks.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                adapter.submitList(trucks)
            }
        })

        viewModel.balanceRefreshed.observe(this, Observer { (tagId, newBalance) ->
            // Update the item in the list
            val currentList = adapter.currentList.toMutableList()
            val index = currentList.indexOfFirst { it.fastagTagId == tagId }
            if (index != -1) {
                currentList[index].fastagBalance = newBalance
                adapter.submitList(currentList)
                adapter.notifyItemChanged(index)
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            errorMsg?.let {
                uiUtils.showSnackbar(it)
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchFastagTrucks()
        }
    }
}

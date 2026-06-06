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
import com.delhivery.axle.ui.fastag.issuance.BuyFasTagActivity
import com.delhivery.axle.ui.fastag.tagAssignment.pendingActions.PendingActionsActivity
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
                    it.fastagVrn.equals(vehicleNumber, ignoreCase = true)
                }
                truck?.let {
                    val intent = Intent(this, FastagTransactionDetailsActivity::class.java).apply {
                        putExtra(FastagTransactionDetailsActivity.EXTRA_TAG_ID, it.fastagId)
                        putExtra(FastagTransactionDetailsActivity.EXTRA_VEHICLE_NUMBER, it.fastagVrn)
                        putExtra(FastagTransactionDetailsActivity.BALANCE, it.fastagBalance ?: "0")
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
        setupPendingActionsObservers()

        viewModel.fetchFastagTrucks()
        viewModel.fetchFastagPendingCount()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentLl)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivSearch.setOnClickListener {
            val vehicleNumbers = ArrayList(
                adapter.currentList.mapNotNull { it.fastagVrn }
            )
            searchLauncher.launch(FastagSearchActivity.newIntent(this, vehicleNumbers))
        }
    }

    private fun setupRecyclerView() {
        adapter = FastagTruckAdapter(
            onRechargeClick = { truck ->
                val intent = Intent(this, FastagRechargeActivity::class.java).apply {
                    putExtra(FastagRechargeActivity.TAG_ID, truck.fastagId)
                    putExtra(FastagRechargeActivity.VEHICLE_NUMBER, truck.fastagVrn)
                    putExtra(FastagRechargeActivity.FASTAG_BALANCE, truck.fastagBalance ?: "0")
                }
                startActivity(intent)
            },
            onDetailsClick = { truck ->
                val intent = Intent(this, FastagTransactionDetailsActivity::class.java).apply {
                    putExtra(FastagTransactionDetailsActivity.EXTRA_TAG_ID, truck.fastagId)
                    putExtra(FastagTransactionDetailsActivity.EXTRA_VEHICLE_NUMBER, truck.fastagVrn)
                    putExtra(FastagTransactionDetailsActivity.BALANCE, truck.fastagBalance ?: "0")
                }
                startActivity(intent)
            },
            onRefreshClick = { truck ->
                truck.fastagId?.let { tagId ->
                    uiUtils.showProgress()
                    viewModel.refreshBalance(tagId)
                }
            },
            onItemClick = { truck ->
                val intent = Intent(this, FastagTransactionDetailsActivity::class.java).apply {
                    putExtra(FastagTransactionDetailsActivity.EXTRA_TAG_ID, truck.fastagId)
                    putExtra(FastagTransactionDetailsActivity.EXTRA_VEHICLE_NUMBER, truck.fastagVrn)
                    putExtra(FastagTransactionDetailsActivity.BALANCE, truck.fastagBalance ?: "0")
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
           startActivity(Intent(this@FastagTrucksActivity, BuyFasTagActivity::class.java))
        }
        binding.btnEmptyBuyFastag.setOnClickListener {
            startActivity(Intent(this@FastagTrucksActivity, BuyFasTagActivity::class.java))
        }
        binding.fastagPendingCard.setOnClickListener {
            startActivity(PendingActionsActivity.newIntent(this))
        }
    }

    private fun showBuyFastagDialog() {
        val dialog =
            BuyFastagBottomSheetDialogFragment.newInstance { selectedCity, truckCount ->
                // Handle buy FASTag submission
                uiUtils.showSnackbar("FASTag request submitted")
            }
        dialog.show(supportFragmentManager, "BuyFastagBottomSheet")
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
                binding.ivSearch.visibility = View.GONE
                binding.tvSectionTitle.visibility = View.GONE
                binding.bannerCard.visibility = View.GONE
            } else {
                binding.ivSearch.visibility = View.VISIBLE
                binding.bannerCard.visibility = View.VISIBLE
                binding.rvFastagTrucks.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.tvSectionTitle.visibility = View.VISIBLE
                binding.bannerCard.visibility = View.VISIBLE
                adapter.submitList(trucks)
            }
        })

        viewModel.balanceRefreshed.observe(this, Observer { (tagId, newBalance) ->
            uiUtils.hideProgress()
            // Update the item in the list
            val currentList = adapter.currentList.toMutableList()
            val index = currentList.indexOfFirst { it.fastagId == tagId }
            if (index != -1) {
                val updatedItem = currentList[index].copy(fastagBalance = newBalance)
                currentList[index] = updatedItem
                adapter.submitList(currentList)
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            uiUtils.hideProgress()
            errorMsg?.let {
                uiUtils.showSnackbar(it)
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchFastagTrucks()
            viewModel.fetchFastagPendingCount()
        }
    }

    private fun setupPendingActionsObservers() {
        // Shimmer loading state
        viewModel.fastagPendingLoading.observe(this) { loading ->
            if (loading) {
                binding.fastagPendingShimmer.alpha = 0f
                binding.fastagPendingShimmer.visibility = View.VISIBLE
                binding.fastagPendingShimmer.animate().alpha(1f).setDuration(200).start()
            } else {
                binding.fastagPendingShimmer.animate().alpha(0f).setDuration(200).withEndAction {
                    binding.fastagPendingShimmer.visibility = View.GONE
                }.start()
            }
        }

        // Pending count → show/hide card with animation
        viewModel.fastagPendingCount.observe(this) { count ->
            if (count > 0) {
                binding.fastagPendingCard.alpha = 0f
                binding.fastagPendingCard.visibility = View.VISIBLE
                binding.fastagPendingCard.animate().alpha(1f).setDuration(300).start()
                binding.tvFastagPendingText.text =
                    getString(R.string.label_fastag_pending_actions, count)
            } else {
                binding.fastagPendingCard.animate().alpha(0f).setDuration(200).withEndAction {
                    binding.fastagPendingCard.visibility = View.GONE
                }.start()
            }
        }
    }
}

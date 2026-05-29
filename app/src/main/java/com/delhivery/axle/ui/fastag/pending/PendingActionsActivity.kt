package com.delhivery.axle.ui.fastag.pending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityPendingActionsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils

class PendingActionsActivity : BaseActivity<ActivityPendingActionsBinding, PendingActionsViewModel>() {

    override fun getViewModelClass() = PendingActionsViewModel::class.java
    override fun layoutId() = R.layout.activity_pending_actions
    override fun requireConnection() = true

    private lateinit var adapter: PendingOrdersAdapter

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PendingActionsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchPendingOrders()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = PendingOrdersAdapter()
        binding.rvPendingOrders.apply {
            layoutManager = LinearLayoutManager(this@PendingActionsActivity)
            adapter = this@PendingActionsActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this, Observer { loading ->
            binding.shimmerLayout.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) {
                binding.rvPendingOrders.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
            }
        })

        viewModel.pendingOrders.observe(this, Observer { orders ->
            if (orders.isNullOrEmpty()) {
                binding.rvPendingOrders.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvPendingOrders.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                adapter.submitList(orders)
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            errorMsg?.let { uiUtils.showSnackbar(it) }
        })
    }
}

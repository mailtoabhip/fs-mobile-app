package com.delhivery.axle.ui.fastag

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DisputeType
import com.delhivery.axle.databinding.ActivityFastagDisputeIssuesBinding
import com.delhivery.axle.ui.base.BaseActivity

class FastagDisputeIssuesActivity : BaseActivity<ActivityFastagDisputeIssuesBinding, FastagDisputeIssuesViewModel>() {

    override fun getViewModelClass() = FastagDisputeIssuesViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_dispute_issues
    override fun requireConnection() = true

    private lateinit var adapter: DisputeIssuesAdapter
    private var partner: String = "IDFC"
    private var fastagId: String = ""
    private var tollPlazaId: String = ""
    private var txnId: String = ""

    companion object {
        const val EXTRA_PARTNER = "partner"
        const val EXTRA_TXN_ID = "txn_id"
        const val EXTRA_FASTAG_ID = "fastag_id"
        const val TOLL_PLAZA_ID = "toll_plaza_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        observeData()
        loadDisputeIssues()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupUI() {
        partner = intent.getStringExtra(EXTRA_PARTNER) ?: "IDFC"
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(TOLL_PLAZA_ID) ?: ""
        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""

        binding.tvTitle.text = "Fastag related issues"
        binding.ivBack.setOnClickListener {
            finish()
        }

        adapter = DisputeIssuesAdapter { disputeType ->
            onDisputeTypeSelected(disputeType)
        }

        binding.rvDisputeIssues.apply {
            layoutManager = LinearLayoutManager(this@FastagDisputeIssuesActivity)
            adapter = this@FastagDisputeIssuesActivity.adapter
        }
    }

    private fun loadDisputeIssues() {
        viewModel.getDisputeIssues(partner)
    }

    private fun observeData() {
        viewModel.progressData.observe(this) { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
                binding.rvDisputeIssues.visibility = View.GONE
            } else {
                uiUtils.hideProgress()
                binding.rvDisputeIssues.visibility = View.VISIBLE
            }
        }

        viewModel.disputeIssuesData.observe(this) { response ->
            response.disputeTypes?.let { issues ->
                adapter.submitList(issues)
            }
        }

    }

    private fun onDisputeTypeSelected(disputeType: DisputeType) {
        if (disputeType.addTxnReq == true) {
            // Requires transaction selection - go to transaction selection screen
            val intent = Intent(this, FastagTransactionSelectionActivity::class.java).apply {
                putExtra(FastagTransactionSelectionActivity.EXTRA_TITLE, disputeType.displayName ?: "")
                putExtra(FastagTransactionSelectionActivity.EXTRA_SUBTITLE, disputeType.subTitle ?: "")
                putExtra(FastagTransactionSelectionActivity.EXTRA_PARTNER, partner)
                putExtra(FastagTransactionSelectionActivity.EXTRA_DISPUTE_CODE, disputeType.code)
                putExtra(FastagTransactionSelectionActivity.EXTRA_FASTAG_ID, fastagId)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_TOLL_PLAZA_ID, tollPlazaId)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_TXN_ID, txnId)

            }
            startActivity(intent)
        } else {
            // No transaction required - go directly to dispute form
            val intent = Intent(this, FastagDynamicDisputeFormActivity::class.java).apply {
                putExtra(FastagDynamicDisputeFormActivity.DISPUTE_TITLE, disputeType.displayName)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_DISPUTE_TYPE_CODE, disputeType.code)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_FASTAG_ID, fastagId)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_SHOW_TRANSACTION, false)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_TOLL_PLAZA_ID, tollPlazaId)
                putExtra(FastagDynamicDisputeFormActivity.EXTRA_TXN_ID, txnId)

            }
            startActivity(intent)
        }
    }
}

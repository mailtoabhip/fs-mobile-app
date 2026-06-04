package com.delhivery.axle.ui.fastag.qdr

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DisputeType
import com.delhivery.axle.databinding.ActivityFastagDisputeIssuesBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.qdr.DisputeIssuesAdapter
import com.delhivery.axle.ui.fastag.qdr.FastagDisputeIssuesViewModel
import com.delhivery.axle.ui.fastag.qdr.FastagDynamicDisputeFormActivity
import com.delhivery.axle.ui.fastag.qdr.FastagTransactionSelectionActivity
import com.delhivery.axle.utils.WindowInsetsUtils

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
        private const val REQCODE_DISPUTE_FORM = 1002
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
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
    }

    private fun setupUI() {
        partner = intent.getStringExtra(EXTRA_PARTNER) ?: "IDFC"
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(TOLL_PLAZA_ID) ?: ""
        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_DISPUTE_FORM -> {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun onDisputeTypeSelected(disputeType: DisputeType) {
        if (disputeType.addTxnReq == true) {
            // Requires transaction selection - go to transaction selection screen
            val intent = Intent(this, FastagTransactionSelectionActivity::class.java).apply {
                putExtra(FastagTransactionSelectionActivity.Companion.EXTRA_TITLE, disputeType.displayName ?: "")
                putExtra(FastagTransactionSelectionActivity.Companion.EXTRA_SUBTITLE, disputeType.subTitle ?: "")
                putExtra(FastagTransactionSelectionActivity.Companion.EXTRA_PARTNER, partner)
                putExtra(FastagTransactionSelectionActivity.Companion.EXTRA_DISPUTE_CODE, disputeType.code)
                putExtra(FastagTransactionSelectionActivity.Companion.EXTRA_FASTAG_ID, fastagId)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_TOLL_PLAZA_ID, tollPlazaId)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_TXN_ID, txnId)

            }
            startActivityForResult(intent, REQCODE_DISPUTE_FORM)
        } else {
            // No transaction required - go directly to dispute form
            val intent = Intent(this, FastagDynamicDisputeFormActivity::class.java).apply {
                putExtra(FastagDynamicDisputeFormActivity.Companion.DISPUTE_TITLE, disputeType.displayName)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_DISPUTE_TYPE_CODE, disputeType.code)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_FASTAG_ID, fastagId)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_SHOW_TRANSACTION, false)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_TOLL_PLAZA_ID, tollPlazaId)
                putExtra(FastagDynamicDisputeFormActivity.Companion.EXTRA_TXN_ID, txnId)

            }
            startActivityForResult(intent, REQCODE_DISPUTE_FORM)
        }
    }
}
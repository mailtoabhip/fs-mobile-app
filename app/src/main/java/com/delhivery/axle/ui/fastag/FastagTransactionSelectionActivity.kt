package com.delhivery.axle.ui.fastag

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagTransactionSelectionBinding
import com.delhivery.axle.ui.base.BaseActivity

class FastagTransactionSelectionActivity : BaseActivity<ActivityFastagTransactionSelectionBinding, FastagTransactionSelectionViewModel>() {

    override fun getViewModelClass() = FastagTransactionSelectionViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_transaction_selection
    override fun requireConnection() = true

    private lateinit var adapter: TransactionSelectionAdapter
    private var title: String = ""
    private var subTitle: String = ""
    private var fastagId: String = ""
    private var tollPlazaId: String = ""
    private var disputeTypeCode: String = ""
    private var txnId: String = ""

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_SUBTITLE = "subtitle"
        const val EXTRA_PARTNER = "partner"
        const val EXTRA_DISPUTE_CODE = "dispute_code"
        const val EXTRA_FASTAG_ID = "fastag_id"
        const val EXTRA_TOLL_PLAZA_ID = "toll_plaza_id"
        const val EXTRA_TXN_ID = "txn_id"
        private const val REQCODE_DISPUTE_FORM = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        observeData()
        loadTransactions()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupUI() {
        title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        subTitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(EXTRA_TOLL_PLAZA_ID) ?: ""
        disputeTypeCode = intent.getStringExtra(EXTRA_DISPUTE_CODE) ?: ""
        txnId = intent.getStringExtra(EXTRA_TXN_ID) ?: ""

        binding.tvTitle.text = "Fastag related issues"

        binding.ivBack.setOnClickListener {
            finish()
        }

        adapter = TransactionSelectionAdapter { transaction ->
            onTransactionSelected(transaction)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@FastagTransactionSelectionActivity)
            adapter = this@FastagTransactionSelectionActivity.adapter
        }

        // Initially disable the confirm button
        binding.btnConfirmSelection.isEnabled = false
        binding.btnConfirmSelection.alpha = 0.3f

        binding.btnConfirmSelection.setOnClickListener {
            val selectedTransaction = adapter.getSelectedTransaction()
            if (selectedTransaction != null) {
                // Navigate to dynamic dispute form
                val intent = Intent(this, FastagDynamicDisputeFormActivity::class.java).apply {
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_DISPUTE_TYPE_CODE, disputeTypeCode)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_SELECTED_TRANSACTION_ID, selectedTransaction.id)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_FASTAG_ID, fastagId)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TOLL_PLAZA_ID, selectedTransaction.tollPlazaId ?: "")
                    putExtra(FastagDynamicDisputeFormActivity.DISPUTE_TITLE, title)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_SUBTITLE, subTitle)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_TOLL_NAME, selectedTransaction.tollName)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_TIMESTAMP, selectedTransaction.timestamp)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TRANSACTION_AMOUNT, selectedTransaction.amount ?: 0.0)
                    putExtra(FastagDynamicDisputeFormActivity.EXTRA_TXN_ID, txnId)
                }
                startActivityForResult(intent, REQCODE_DISPUTE_FORM)
            }
        }
    }

    private fun loadTransactions() {
        viewModel.getTransactionsByTollPlaza(tollPlazaId = tollPlazaId, fastagId = fastagId, txnId = txnId)
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

    private fun observeData() {
        viewModel.progressData.observe(this) { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
                binding.layoutContent.visibility = View.GONE
                binding.layoutButtonContainer.visibility = View.GONE
            } else {
                uiUtils.hideProgress()
                binding.layoutContent.visibility = View.VISIBLE
                binding.layoutButtonContainer.visibility = View.VISIBLE
            }
        }

        viewModel.transactionsByTollPlazaData.observe(this) { response ->
            response.transactions?.let { transactions ->
                if (transactions.isEmpty()) {
                    binding.rvTransactions.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.tvTransactionTitle.visibility = View.GONE
                    binding.tvTransactionSubtitle.visibility = View.GONE
                    binding.btnConfirmSelection.text = "Select different issue type"
                    binding.btnConfirmSelection.isEnabled = true
                    binding.btnConfirmSelection.alpha = 1.0f
                    binding.layoutButtonContainer.visibility = View.VISIBLE
                    binding.btnConfirmSelection.setOnClickListener { finish() }
                } else {
                    binding.rvTransactions.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    val transactionItems = transactions.map { txn ->
                        TransactionItem(
                            id = txn.txnId ?: "",
                            tollName = txn.tollPlazaName ?: "Unknown",
                            timestamp = formatDateTime(txn.txnDateTime ?: ""),
                            amount = if (txn.txnType?.toLowerCase() == "debit") -(txn.txnAmount ?: 0.0) else (txn.txnAmount ?: 0.0),
                            tollPlazaId = txn.tollPlazaId
                        )
                    }
                    adapter.submitList(transactionItems)
                }
            } ?: run {
                binding.rvTransactions.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.tvTransactionTitle.visibility = View.GONE
                binding.tvTransactionSubtitle.visibility = View.GONE
                binding.btnConfirmSelection.text = "Select different issue type"
                binding.btnConfirmSelection.isEnabled = true
                binding.btnConfirmSelection.alpha = 1.0f
                binding.layoutButtonContainer.visibility = View.VISIBLE
                binding.btnConfirmSelection.setOnClickListener { finish() }
            }
        }
    }

    private fun formatDateTime(dateTime: String): String {
        if (dateTime.isEmpty()) return ""
        return com.delhivery.axle.utils.DateUtils.formatFastagTransactionDate(dateTime)
    }

    private fun onTransactionSelected(transaction: TransactionItem) {
        adapter.setSelectedTransaction(transaction)
        // Enable confirm button when a transaction is selected
        binding.btnConfirmSelection.isEnabled = true
        binding.btnConfirmSelection.alpha = 1.0f
    }
}

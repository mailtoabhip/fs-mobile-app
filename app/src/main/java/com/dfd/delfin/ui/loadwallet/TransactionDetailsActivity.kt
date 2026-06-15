package com.dfd.delfin.ui.loadwallet

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityTransactionDetailsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.StringUtils
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.extensions.getSerializable

class TransactionDetailsActivity :
    BaseActivity<ActivityTransactionDetailsBinding, TransactionDetailsViewModel>() {

    companion object {
        private const val EXTRA_TXN_DATA = "extra_txn_data"
    }

    private var currentData: WalletHistoryItemData? = null

    override fun getViewModelClass() = TransactionDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_transaction_details
    override fun requireConnection() = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        observeRefreshStatus()

        val txnData = intent.getSerializable(EXTRA_TXN_DATA, WalletHistoryItemData::class.java)
        txnData?.let { bindData(it) }
    }

    private fun observeRefreshStatus() {
        viewModel.refreshStatusLiveData.observe(this) { result ->
            uiUtils.hideProgress()
            if (result != null) {
                currentData?.let { data ->
                    val updated = data.copy(status = result.second)
                    currentData = updated
                    bindData(updated)
                }
            }
        }

        viewModel.refreshErrorLiveData.observe(this) { errorMsg ->
            uiUtils.hideProgress()
            errorMsg?.let { dialogUtils.showErrorDialog(it, 3L) }
        }
    }

    private fun bindData(data: WalletHistoryItemData) {
        currentData = data
        binding.textTxnId.text = "TXID: ${data.txnNumber}"

        // Status badge — use detail-specific label
        binding.textStatus.text = data.detailStatusLabel()
        binding.textStatus.setTextColor(ContextCompat.getColor(this, data.statusColorRes()))
        val badgeBg = binding.textStatus.background as? GradientDrawable
        badgeBg?.setColor(getStatusBgColor(data.status))

        // Amount
        val sign = if (data.type.lowercase().contains("credit")) "+" else "-"
        binding.textAmount.text = "${sign}₹${StringUtils.formatAmount(data.amount)}"

        // Pending: show refresh icon, default amount color
        // Failed: strikethrough + grey color
        // Success: default amount color
        when {
            data.isPending() -> {
                binding.textAmount.setTextColor(ContextCompat.getColor(this, R.color.title_black))
                binding.iconRefreshAmount.visibility = View.VISIBLE
                binding.iconRefreshAmount.setOnClickListener {
                    uiUtils.showProgress()
                    viewModel.fetchTransactionStatus(data.txnNumber, data.dateTime)
                }
            }
            data.isFailed() -> {
                binding.textAmount.setTextColor(ContextCompat.getColor(this, R.color.title_black))
                binding.iconRefreshAmount.visibility = View.GONE
            }
            else -> {
                binding.textAmount.setTextColor(ContextCompat.getColor(this, R.color.title_black))
                binding.iconRefreshAmount.visibility = View.GONE
            }
        }

        // Reason & Date
        binding.textReason.text = data.transactionReason?.takeIf { it.isNotBlank() } ?: data.title
        binding.textDate.text = DateUtils.getUtcToIstFormatTime(data.dateTime) ?: ""

        // Message text and color per status
        when {
            data.isFailed() -> {
                binding.textMessage.text = getString(R.string.label_txn_failed_message)
                binding.textMessage.setTextColor(ContextCompat.getColor(this, R.color.status_lost_bid))
            }
            data.isPending() -> {
                binding.textMessage.text = getString(R.string.label_txn_pending_message)
                binding.textMessage.setTextColor(ContextCompat.getColor(this, R.color.pending_status))
            }
            data.type.lowercase().contains("credit") -> {
                binding.textMessage.text = getString(R.string.label_txn_credit_message)
                binding.textMessage.setTextColor(ContextCompat.getColor(this, R.color.text_dark_grey))
            }
            else -> {
                binding.textMessage.text = getString(R.string.label_txn_success_message)
                binding.textMessage.setTextColor(ContextCompat.getColor(this, R.color.text_dark_grey))
            }
        }
    }

    private fun getStatusBgColor(status: String): Int = when (status.lowercase()) {
        "success" -> Color.parseColor("#EFF6FF")
        "pending" -> Color.parseColor("#FEF3C7")
        "failure","failed" -> Color.parseColor( "#FEF2F2")
        else -> Color.parseColor("#F3F4F6")
    }
}

fun transactionDetailsIntent(context: Context, data: WalletHistoryItemData): Intent {
    return Intent(context, TransactionDetailsActivity::class.java).apply {
        putExtra("extra_txn_data", data)
    }
}

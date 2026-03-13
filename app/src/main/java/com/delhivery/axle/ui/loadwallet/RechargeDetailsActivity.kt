package com.delhivery.axle.ui.loadwallet

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityRechargeDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getSerializable

class RechargeDetailsActivity :
    BaseActivity<ActivityRechargeDetailsBinding, RechargeDetailsViewModel>() {

    companion object {
        private const val EXTRA_RECHARGE_DATA = "extra_recharge_data"
    }

    private var currentData: WalletHistoryItemData? = null

    override fun getViewModelClass() = RechargeDetailsViewModel::class.java
    override fun layoutId() = R.layout.activity_recharge_details
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

        val data = intent.getSerializable(EXTRA_RECHARGE_DATA, WalletHistoryItemData::class.java)
        data?.let { bindData(it) }
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

        // Status badge
        binding.textStatus.text = data.detailStatusLabel()
        binding.textStatus.setTextColor(ContextCompat.getColor(this, data.statusColorRes()))
        val badgeBg = binding.textStatus.background as? GradientDrawable
        badgeBg?.setColor(getStatusBgColor(data.status))

        // Amount — always +₹ for recharges
        binding.textAmount.text = "+₹${StringUtils.formatAmount(data.amount)}"

        when {
            data.isPending() -> {
                binding.textAmount.setTextColor(ContextCompat.getColor(this, R.color.title_black))
                binding.iconRefreshAmount.visibility = View.VISIBLE
                binding.iconRefreshAmount.setOnClickListener {
                    uiUtils.showProgress()
                    viewModel.fetchRechargeStatus(data.txnNumber, data.dateTime)
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

        // Recharge Date
        binding.textDate.text = DateUtils.formatISODate(data.dateTime, "dd MMM yy, h:mm a")

        // Bank Reference no
        binding.textBankRef.text = data.bankReferenceNo?.takeIf { it.isNotBlank() } ?: "-"

        // Added via
        binding.textAddedVia.text = data.addedVia?.takeIf { it.isNotBlank() } ?: "-"

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
            else -> {
                binding.textMessage.visibility = View.GONE
            }
        }
    }

    private fun getStatusBgColor(status: String): Int = when (status.lowercase()) {
        "success" -> Color.parseColor("#EFF6FF")
        "pending" -> Color.parseColor("#FEF3C7")
        "failure","failed" -> Color.parseColor("#FEF2F2")
        else -> Color.parseColor("#F3F4F6")
    }

}

fun rechargeDetailsIntent(context: Context, data: WalletHistoryItemData): Intent {
    return Intent(context, RechargeDetailsActivity::class.java).apply {
        putExtra("extra_recharge_data", data)
    }
}

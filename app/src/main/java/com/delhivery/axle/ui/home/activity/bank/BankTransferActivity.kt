package com.delhivery.axle.ui.home.activity.bank

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.ActivityBankTrasnferBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty

class BankTransferActivity : BaseActivity<ActivityBankTrasnferBinding, BankTransferViewModel>() {

  override fun getViewModelClass() = BankTransferViewModel::class.java

  override fun layoutId() = R.layout.activity_bank_trasnfer

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Transfer to bank account"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.walletLiveData.observe(this, Observer {
      binding.wallet = it
      binding.bank = viewModel.bankname
      binding.executePendingBindings()
      binding.progress.root.visibility = View.GONE
      binding.containerBank.visibility = View.VISIBLE
    })

    viewModel.transactionLiveData.observe(this, Observer {
      binding.containerStatus.visibility = View.VISIBLE
      if (it.isNotNullOrEmpty()) {
        binding.labelTransaction.text = getString(string.label_transfer_success)
        binding.imgStatus.setImageResource(R.drawable.ic_success)
        binding.textStatus.text = getString(string.label_in_process)
        binding.textStatus.setTextColor(ContextCompat.getColor(this, R.color.status_confirmed))
      } else {
        binding.labelTransaction.text = getString(string.label_transfer_failed)
        binding.imgStatus.setImageResource(R.drawable.ic_cancel)
        binding.textStatus.text = getString(string.label_failed)
        binding.textStatus.setTextColor(ContextCompat.getColor(this, R.color.status_lost))
      }
    })

    binding.btnTransfer.setOnClickListener { processTransfer() }
    binding.btnBack.setOnClickListener {
      if (!TextUtils.isEmpty(viewModel.refNumber)) {
        setResult(RESULT_OK)
      }
      finish()
    }
    binding.btnTransactionSummary.setOnClickListener { openTransactions() }

    viewModel.fetchWalletData()
  }

  private fun openTransactions() {
    navigationUtils.navigate(transactionsIntent(this), true)
  }

  private fun processTransfer() {
    val amount: Int
    try {
      amount = binding.editAmount.text.toString()
          .toInt()
      if (amount == 0) {
        throw NumberFormatException()
      } else if (amount > viewModel.wallet.balance) {
        throw Exception("Max ${viewModel.wallet.balance()} can be transferred to bank")
      }
    } catch (e: java.lang.NumberFormatException) {
      binding.tilAmount.error = "Please enter valid amount"
      binding.editAmount.requestFocus()
      return
    } catch (e: Exception) {
      binding.tilAmount.error = e.message
      binding.editAmount.requestFocus()
      return
    }

    val mAmount = "₹ $amount"
    binding.amount.text = mAmount
    viewModel.transferToBank(amount)
  }

}

/**
 * Bank Transfer intent
 */
fun bankTransferIntent(
  context: Context
) = Intent(context, BankTransferActivity::class.java).apply {
}
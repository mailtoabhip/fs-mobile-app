package com.dfd.delfin.ui.home.activity.transactiondetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.data.transactions.TransactionsItemData
import com.dfd.delfin.databinding.ActivityTransactionDetailBinding
import com.dfd.delfin.databinding.ViewAdvanceAutoDebitTransactionBinding
import com.dfd.delfin.databinding.ViewAdvanceCreditTransactionBinding
import com.dfd.delfin.databinding.ViewBankTransferTransactionBinding
import com.dfd.delfin.databinding.ViewBidDetailsLoadingBidsBinding
import com.dfd.delfin.databinding.ViewDebitNoteTransactionBinding
import com.dfd.delfin.databinding.ViewFuelCashbackCreditTransactionBinding
import com.dfd.delfin.databinding.ViewFuelCashbackDebitTransactionBinding
import com.dfd.delfin.databinding.ViewFuelCreditRevertTransactionBinding
import com.dfd.delfin.databinding.ViewFuelDebitTransactionBinding
import com.dfd.delfin.databinding.ViewReconciliationDebitTransactionBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionState
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateAdvanceAutoDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateAdvanceCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateBankTransfer
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateDebitNote
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelCashbackCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelCashbackDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelDebit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateFuelRevertCredit
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateLoading
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionStateReconciliationDebit
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.extensions.getSerializable
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Handles transactions details for bank transactions
 */
class TransactionDetailActivity : BaseActivity<ActivityTransactionDetailBinding, TransactionDetailViewModel>() {

  @Inject lateinit var userPrefs: UserPrefs

  override fun getViewModelClass() = TransactionDetailViewModel::class.java

  override fun layoutId() = R.layout.activity_transaction_detail

  override fun requireConnection() = true

  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("TransactionDetailActivity_SetupTime")
    activitySetupTrace?.start()
    /* validate intent */
    if (intent == null || !intent.hasExtra(ARGS_TRANSACTION_DATA)) {
      throw IllegalArgumentException("Required data $ARGS_TRANSACTION_DATA not found")
    }

    viewModel.transaction = intent.getSerializable(ARGS_TRANSACTION_DATA, TransactionsItemData::class.java)!!
    binding.transaction = viewModel.transaction
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }

      title = viewModel.transaction.transactionHeading()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })

    viewModel.transactionStateLiveData.observe(this, TransactionObserver())
    viewModel.transactionStateLiveData.postValue(TransactionStateLoading())

    viewModel.updateDetails()
  }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

  /**
   * Observes [TransactionState]
   */
  inner class TransactionObserver : Observer<TransactionState?> {
    override fun onChanged(t: TransactionState?) {
      t?.let { state ->
        when (state) {
          is TransactionStateLoading -> {
            ViewBidDetailsLoadingBidsBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply { }
          }

          is TransactionStateBankTransfer -> {
            ViewBankTransferTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  bank = viewModel.bank
                }
          }

          is TransactionStateFuelRevertCredit -> {
            ViewFuelCreditRevertTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.tripAndFuel.first
                }
          }

          is TransactionStateFuelDebit -> {
            ViewFuelDebitTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateAdvanceCredit -> {
            ViewAdvanceCreditTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateFuelCashbackCredit -> {
            ViewFuelCashbackCreditTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateReconciliationDebit -> {
            ViewReconciliationDebitTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateFuelCashbackDebit -> {
            ViewFuelCashbackDebitTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateAdvanceAutoDebit -> {
            ViewAdvanceAutoDebitTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          is TransactionStateDebitNote -> {
            ViewDebitNoteTransactionBinding.inflate(
                layoutInflater, binding.containerDetail, false
            )
                .apply {
                  transaction = state.transaction
                  trip = state.trip
                }
          }

          else -> null
        }?.let { _binding ->
          binding.containerDetail.apply {
            removeAllViews()
            addView(_binding.root)
          }
        }
      }
    }
  }

}

private const val ARGS_TRANSACTION_DATA = "args_transaction_data"

/**
 * Transaction Detail intent
 */
fun transactionDetailIntent(
  context: Context,
  transaction: TransactionsItemData?
) = Intent(context, TransactionDetailActivity::class.java).apply {
  putExtra(ARGS_TRANSACTION_DATA, transaction)
}
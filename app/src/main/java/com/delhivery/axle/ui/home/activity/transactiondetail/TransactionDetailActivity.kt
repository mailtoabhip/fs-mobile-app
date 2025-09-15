package com.delhivery.axle.ui.home.activity.transactiondetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.databinding.ActivityTransactionDetailBinding
import com.delhivery.axle.databinding.ViewAdvanceAutoDebitTransactionBinding
import com.delhivery.axle.databinding.ViewAdvanceCreditTransactionBinding
import com.delhivery.axle.databinding.ViewBankTransferTransactionBinding
import com.delhivery.axle.databinding.ViewBidDetailsLoadingBidsBinding
import com.delhivery.axle.databinding.ViewDebitNoteTransactionBinding
import com.delhivery.axle.databinding.ViewFuelCashbackCreditTransactionBinding
import com.delhivery.axle.databinding.ViewFuelCashbackDebitTransactionBinding
import com.delhivery.axle.databinding.ViewFuelCreditRevertTransactionBinding
import com.delhivery.axle.databinding.ViewFuelDebitTransactionBinding
import com.delhivery.axle.databinding.ViewReconciliationDebitTransactionBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionState
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateAdvanceAutoDebit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateAdvanceCredit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateBankTransfer
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateDebitNote
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelCashbackCredit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelCashbackDebit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelDebit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateFuelRevertCredit
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateLoading
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionStateReconciliationDebit
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
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
  inner class TransactionObserver : Observer<TransactionState> {
    override fun onChanged(t: TransactionState) {
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
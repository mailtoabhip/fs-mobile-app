package com.delhivery.orion.ui.biddetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.databinding.ActivityBidDetailsBinding
import com.delhivery.orion.databinding.ViewBidDetailsConfirmedBidBinding
import com.delhivery.orion.databinding.ViewBidDetailsEditBidBinding
import com.delhivery.orion.databinding.ViewBidDetailsLoadingBidsBinding
import com.delhivery.orion.databinding.ViewBidDetailsPlaceBidBinding
import com.delhivery.orion.databinding.ViewBidDetailsPlaceBidFirstBinding
import com.delhivery.orion.databinding.ViewBidDetailsRejectedBidBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.utils.extensions.visible

class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>() {

  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_bid_details

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    if (intent == null || !intent.hasExtra(TransactionIdIntentKey)) {
      throw IllegalArgumentException("Required data $TransactionIdIntentKey not found")
    }

    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Orion"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* setup live data observers */
    viewModel.transactionLiveData.observe(this, TransactionObserver())
    viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())

    /* fetch transaction details */
    viewModel.fetchTransactionDetails()
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
    override fun onChanged(t: HomeBidsRequestItemData?) {
      t?.let { _transaction ->
        binding.transaction = _transaction
        title =
          "${_transaction.tripDisplayName()} (${_transaction.requiredAt()})"
      }
    }
  }

  /**
   * Transaction bid details UI updation observer
   */
  inner class TransactionBidObserver : Observer<BidDetailsUserBidState> {
    override fun onChanged(t: BidDetailsUserBidState?) {
      t?.let { state ->
        when (state) {
          is BidDetailsUserBidState_PlaceBidFirst -> {
            ViewBidDetailsPlaceBidFirstBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                .apply {
                  btnPlaceBid.setOnClickListener { bidDialog() }
                }
          }
          is BidDetailsUserBidState_PlaceBid -> {
            ViewBidDetailsPlaceBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  bidsRecieved = state.bidsCount
                  btnPlaceBid.setOnClickListener { bidDialog() }
                }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  bidsRecieved = state.bidsCount
                  textUserBidAmount.text =
                    getString(R.string.label_user_bid_amount, state.userBid.bidAmount)
                  textUserBidAmountDiff.text =
                    state.userBid.targetPriceDiff(binding.transaction?.targetPrice ?: 0)
                  btnEditBid.setOnClickListener { bidDialog(state.userBid) }
                }
          }
          is BidDetailsUserBidState_LoadingBids -> {
            ViewBidDetailsLoadingBidsBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
          }
          is BidDetailsUserBidState_ConfirmedBid -> {
            ViewBidDetailsConfirmedBidBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                .apply {
                  textPickupLocation.text = state.pickupLocation
                }
          }
          is BidDetailsUserBidState_RejectedBid -> {
            ViewBidDetailsRejectedBidBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                .apply {
                  textAcceptedBidValue.text =
                    getString(R.string.msg_accepted_bid_value, state.acceptedBid.bidAmount)
                  textBidAmoutDiff.text =
                    state.acceptedBid.targetPriceDiff(binding.transaction?.targetPrice ?: 0)
                  textUserHighestBid.text =
                    getString(R.string.msg_your_highest_bid, state.userBid.bidAmount)
                }
          }
          else -> null
        }?.let { _binding ->
          /* confirmed banner visibility */
          binding.containerConfirmBid.visible(_binding is ViewBidDetailsConfirmedBidBinding)
          /* bidding ended */
          binding.textBidEnded.visible(_binding is ViewBidDetailsRejectedBidBinding)

          binding.containerActions.apply {
            removeAllViews()
            addView(_binding.root)
          }
        }
      }
    }
  }

  /**
   * Create/edit bid dialog
   */
  private fun bidDialog(bid: TransactionBid? = null) {
    binding.transaction?.let {
      BidDetailsCreateEditDialog(this, it, bid, viewModel).show()
    }
  }
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Bid details intent
 */
fun bidDetailsIntent(
  _data: HomeBidsRequestItemData,
  context: Context
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, _data.key())
}
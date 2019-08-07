package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.ActivityBidDetailsBinding
import com.delhivery.axle.databinding.ViewBidDetailsConfirmedBidBinding
import com.delhivery.axle.databinding.ViewBidDetailsEditBidBinding
import com.delhivery.axle.databinding.ViewBidDetailsLoadingBidsBinding
import com.delhivery.axle.databinding.ViewBidDetailsPlaceBidBinding
import com.delhivery.axle.databinding.ViewBidDetailsPlaceBidFirstBinding
import com.delhivery.axle.databinding.ViewBidDetailsRejectedBidBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.visible

class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>() {

  init {
    hasInlineProgress = true
  }

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
    title = "Axle"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* setup live data observers */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.transactionLiveData.observe(this, TransactionObserver())
    viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())
    viewModel.bidPriceLiveData.observe(this, Observer {
      if (it != null) {
        binding.transaction?.transactionBid = it
        binding.textTargetPriceLabel.text = binding.transaction?.amountLabel()
        binding.textTargetPrice.text = binding.transaction?.bidAmount()
      }
      binding.textTargetPrice.visibility = View.VISIBLE
      binding.textTargetPriceLabel.visibility = View.VISIBLE
    })

    /* fetch transaction details */
    viewModel.fetchTransactionDetails()
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showDelhiveryProgress(
              "Getting details", "This usually takes few seconds to load. please be patient.",
              "This usually takes few seconds to load. please be patient."
          )
          false -> uiUtils.hideDelhiveryProgress()
        }
      }
    }
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
    override fun onChanged(t: HomeBidsRequestItemData?) {
      t?.let { _transaction ->
        binding.transaction = _transaction
        title = _transaction.tripDisplayName()
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
                  lowestBid = when (state.lowestBid) {
                    0, null -> ""
                    else -> "Lowest Bid - ${state.lowestBid}"
                  }
                  btnPlaceBid.setOnClickListener { bidDialog() }
                }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  bidsRecieved = state.bidsCount
                  lowestBid = when (state.lowestBid) {
                    0, null -> ""
                    else -> "Lowest Bid - ${state.lowestBid}"
                  }
                  textUserBidAmount.text =
                    getString(R.string.label_user_bid_amount, state.userBid.bidAmount)
                  textUserBidAmountDiff.text =
                    state.userBid.targetPriceDiff(binding.transaction?.target() ?: 0)
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
                  pickUpLocation =
                    StringUtils.capitalize(state.pickupLocation) ?: getString(string.not_available)
                  vehicleNumber = state.vehicleNumber ?: getString(string.not_available)
                  driverName = StringUtils.capitalize(state.driverDetails?.driverName)
                      ?: getString(string.not_available)
                  driverPhone =
                    state.driverDetails?.driverPhoneNo ?: getString(string.not_available)
                }
          }
          is BidDetailsUserBidState_RejectedBid -> {
            ViewBidDetailsRejectedBidBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                .apply {
                  textBidAmoutDiff.text =
                    state.acceptedBid.targetPriceDiff(
                        binding.transaction?.target() ?: 0
                    )
                  textUserHighestBid.text =
                    getString(R.string.msg_your_highest_bid, state.userBid.bidAmount)
                }
          }
          else -> null
        }?.let { _binding ->
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
      BidDetailsCreateEditDialog(
          this, it, bid, viewModel, analyticsUtil = analyticsUtil
      ).show()
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
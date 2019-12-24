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
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Bid detail screen
 */
class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>() {

  init {
    hasInlineProgress = true
  }

  @Inject lateinit var userPrefs: UserPrefs

  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_bid_details

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    try {
      require(
          !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* setup live data observers */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.transactionLiveData.observe(this, TransactionObserver())
    viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())
    viewModel.bidPriceLiveData.observe(this, Observer {
      if (it != null) {
        binding.transaction?.transactionBid = it
        val visibility =
          if (binding.transaction?.bidAmount().isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.textTargetPrice.visibility = visibility
        binding.textTargetPriceLabel.visibility = visibility
        if (visibility == View.VISIBLE) {
          binding.textTargetPrice.text = binding.transaction?.bidAmount()
          binding.textTargetPriceLabel.text = binding.transaction?.amountLabel()
        }
      }
    })

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    refreshData()
  }

  private fun refreshData() {
    binding.error = false
    viewModel.fetchTransactionDetails()
    binding.executePendingBindings()
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> {
            binding.refreshLayout.isRefreshing = true
            binding.refreshing = true
          }
          false -> {
            binding.refreshLayout.isRefreshing = false
          }
        }
      }
      binding.executePendingBindings()
    }
  }

  /**
   * Transaction details and UI updation Observer
   */
  inner class TransactionObserver : Observer<HomeBidsRequestItemData> {
    override fun onChanged(t: HomeBidsRequestItemData?) {
      binding.refreshing = false
      if (t != null) {
        t.let { _transaction ->
          binding.error = false
          binding.transaction = _transaction
          title = _transaction.tripDisplayName()
        }
      } else {
        binding.error = true
        binding.containerError.title = "Session Time Out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
      }
      binding.executePendingBindings()
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
                  lowestBid = when (state.lowestAndUserBidPair) {
                    null -> ""
                    else -> "Lowest Bid - ₹ ${StringUtils.formatAmount(
                        state.lowestAndUserBidPair.first?.bidAmount ?: 0.0
                    )}" + if (state.isPMTIndent) "/MT" else ""
                  }
                  btnPlaceBid.setOnClickListener { bidDialog() }
                }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  bidsRecieved = state.bidsCount
                  val userBid = state.lowestAndUserBidPair.first
                  val lowestTBid = state.lowestAndUserBidPair.second
                  lowestTBid?.let {
                    if (it.biddingType.compareTo(userBid?.biddingType ?: "") == 0) {
                      lowestBid = when (it) {
                        null -> ""
                        else -> "Lowest Bid - ₹ ${StringUtils.formatAmount(
                            it.bidAmount
                        )}" + if (state.isPMTIndent) "/MT" else ""
                      }
                      if (state.bidsCount > 1) {
                        textUserBidAmountDiff.text =
                          userBid?.diffFromLowestBid(it.bidAmount, state.isPMTIndent)
                      }
                    }
                  }

                  userBid?.bidAmount?.let {
                    val bid = getString(string.label_user_bid_amount) + if (state.isPMTIndent) {
                      StringUtils.formatAmount(it) + "/MT"
                    } else {
                      StringUtils.formatAmount(it)
                    }
                    textUserBidAmount.text = bid
                  }

                  btnEditBid.setOnClickListener { bidDialog(userBid) }
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
                  driverPhone =
                    state.driverDetails?.driverPhoneNo ?: getString(string.not_available)
                }
          }
          is BidDetailsUserBidState_RejectedBid -> {
            ViewBidDetailsRejectedBidBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                .apply {
                  val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                    StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0) + "/MT"
                  } else {
                    StringUtils.formatAmount(state.userBid.bidAmount)
                  }
                  textUserHighestBid.text = bidText
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
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        binding.transaction?.let {
          BidDetailsCreateEditDialog(
              this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs
          ).show()
        }
      }
      UNAPPROVED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_not_approved,
            string.msg_dialog_supplier_not_approved,
            getString(string.label_call_us), getString(string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
      DISABLED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_disabled,
            string.msg_dialog_supplier_disabled,
            getString(string.label_call_us), getString(string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
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
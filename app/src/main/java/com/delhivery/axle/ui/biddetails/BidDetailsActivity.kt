package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.visible
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Bid detail screen
 */
class BidDetailsActivity : BaseActivity<ActivityBidDetailsBinding, BidDetailsViewModel>(), BulkBidsRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

  @Inject lateinit var userPrefs: UserPrefs

  override fun getViewModelClass() = BidDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_bid_details

  override fun requireConnection() = true

  private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }

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
    viewModel.dmtStatus = intent.getStringExtra(RequestTypeIntentKey) ?: ""
    viewModel.fromPage = intent.getBooleanExtra(FromPage, false)
    viewModel.active = intent.getBooleanExtra(ActiveBid, false)
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

    viewModel.userBidsData.observe(this, Observer {
      if (it != null) {
       adapter.operation(it)
      }
    })

    viewModel.truckGetLiveData.observe(this, Observer{
      uiUtils.hideProgress()
      if(it!= null){
        val pageTitle = if(it.second.bulkTransactionBids!= null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
        if(it.second.truckUUID != null) {
          BulkBidDetailsCreateEditDialog(this@BidDetailsActivity, it.second, it.second.bulkTransactionBids, it.first,
            viewModel, it.second.unAllocatedVolume!!, analyticsUtil = analyticsUtil, userPrefs = userPrefs, fromPage = "load_detail", pageTitle = pageTitle
          ).show()
        }
        else{
          Toast.makeText(this, "No Vehicle Types Found",Toast.LENGTH_SHORT).show()

        }
      }
    })

    viewModel.editBulkLiveData.observe(this, Observer {
      if(it.first == 10){
        Toast.makeText(this,"Bids Created Successfully", Toast.LENGTH_SHORT).show()
      }
      if(it.first == 20){
        Toast.makeText(this,"Bids Updated Successfully", Toast.LENGTH_SHORT).show()
      }
      if(it.first == 30){
        Toast.makeText(this,"Bids Deleted Successfully", Toast.LENGTH_SHORT).show()
      }
      if(viewModel.editFlg[0] &&  viewModel.editFlg[1] && viewModel.editFlg[2]){
        viewModel.fetchTransactionBids()
        viewModel.editFlg = mutableListOf(false, false, false)
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
          if(_transaction.isDMTIndent()){
              binding.unallocated= getString(string.unallocated_bulk_order)+(_transaction.unAllocatedVolume)?.toInt()+" MT"
           }

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
                state.lowestAndUserBidPair.second?.let {
                  lowestBid = when (state.lowestAndUserBidPair) {
                    null -> ""
                    else -> "Lowest Bid - ₹ ${StringUtils.formatAmount(
                              state.lowestAndUserBidPair.second?.bidAmount ?: 0.0
                    )}" + if (state.isPMTIndent) "/MT" else ""
                  }
                }
                btnPlaceBid.setOnClickListener { bidDialog() }
              }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  val data = viewModel.transaction as HomeBidsRequestItemData
                  data.numBids = state.bidsCount
                  data.transactionBid = state.lowestAndUserBidPair.first
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
                      data.lowestBid = when (it) {
                        null -> 0.0
                        else -> it.bidAmount
                      }
                    }
                  }
                  request = data

                  if(viewModel.analyticsBucket) {
                    if (data.oneVisibility() == View.VISIBLE || data.twoVisibility() == View.VISIBLE) {
                      analyticsUtil.trackEvent(
                              EVENT_BID_INLINE_PROMPT,
                              mutableListOf(PROPERTY_USER_ID , PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE , PROPERTY_OVERALL_PERFORMANCE),
                              mutableListOf(userPrefs.userId(), data.key() , userPrefs.demandType, userPrefs.userPerformance)
                      )
                    } else if (data.threeVisibility() == View.VISIBLE || data.fourVisibility() == View.VISIBLE) {
                      analyticsUtil.trackEvent(
                              EVENT_BID_REVISE_PROMPT,
                              mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID , PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE),
                              mutableListOf(userPrefs.userId(), data.key(), userPrefs.demandType, userPrefs.userPerformance)
                      )
                    }
                    viewModel.analyticsBucket=false
                  }

                  if(data.threeVisibility() ==View.VISIBLE || data.fourVisibility()==View.VISIBLE) {
                    val mHandler = Handler()
                    var mRunnable :Runnable= Runnable {  }
                    mRunnable = object : Runnable {
                      override fun run() {
                        btnEditBidInsider.setVisibility(View.VISIBLE)
                        //loading our custom made animations
                        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
                        //starting the animation
                        btnEditBidInsider.startAnimation(animation)
                        val animation2 = AnimationUtils.loadAnimation(applicationContext , R.anim.fade_out)
                        btnEditBidInsider.startAnimation(animation2)
                        mHandler.postDelayed({
                          btnEditBidInsider.setVisibility(View.GONE)
                        }, 3000)
                        mHandler.postDelayed(mRunnable,2000)

                      }
                    }
                    mHandler.post(mRunnable)
                  }

                  btnEditBidInsider.setOnClickListener(View.OnClickListener { bidDialog(userBid) })
                  textEditBid.setOnClickListener(View.OnClickListener { bidDialog(userBid) })
                  textEditBid2.setOnClickListener(View.OnClickListener { bidDialog(userBid) })
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
                            StringUtils.capitalize(state.pickupLocation)
                                    ?: getString(string.not_available)
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
          is BidDetailsUserBidState_CancelledBid -> {
            ViewBidDetailsCancelledBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.bidAmount ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                textUserHighestBid.text = bidText
              }
          }

          is BidDetailsUserBidState_BulkLoad_Edit -> {
            ViewBidDetailsBulkLoadEditBinding.inflate(
                    layoutInflater, binding.containerActions, false
            ).apply {
              val data = viewModel.transaction as HomeBidsRequestItemData
              data.bulkTransactionBids = state.bids
              bidsRecieved = state.bidsCount
              rvBidSummary.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BidDetailsActivity.adapter
                (adapter as BulkBidsRVAdapter).clearItems()
                viewModel.getUserBulkBids(state.bids , state.lowestAndUserBidPair.second.let { it!!.bidAmount } )

              }

              btnReviseBidInsider.setOnClickListener{ bidDialog()}
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
          if(viewModel.dmtStatus == "dmt") {
            uiUtils.showProgress()
            viewModel.fetchTruckType(it);
          }
          else{
            BidDetailsCreateEditDialog(
                    this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs , fromPage = "load_detail"
            ).show()
          }
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

  override fun handleAction(actionId: String, position: Int, item: BaseBulkBidSummaryRVAdapterItem<*>) {
    when(actionId){
      EXPAND_CARD -> {
        val bidData = item.data as BulkBidSummaryItemData
        bidData.expanded = !bidData.expanded
        BidDetailsViewModel.truckNumTextViewAdded =!BidDetailsViewModel.truckNumTextViewAdded
        adapter.notifyItemChanged(position)
      }
      OPEN_CONFIRMED_BID -> {

      }
    }
  }

}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
private const val FromPage = "from_page"
private const val ActiveBid= "active_bid"
/**
 * Bid details intent
 */
fun bidDetailsIntent(
  transactionId: String,
  context: Context,
  requestType:String?=null,
  fromBidsPage:Boolean = false,
  active:Boolean = false
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  if(requestType!=null)
  putExtra(RequestTypeIntentKey, requestType)
  putExtra(FromPage,fromBidsPage)
  putExtra(ActiveBid,active)
}
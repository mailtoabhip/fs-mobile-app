package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.delhivery.axle.ui.businessverification.DocUploadAdapter
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.visible
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.text.SimpleDateFormat
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

  var bidEndingTime:String = ""
  var source:String= VALUE_APP_FLOW
  var reviseInitiated:Boolean=false
  var oldAmountbids =""
  var isFirstBid = false
  private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }
  var uploadArray:ArrayList<Pair<String, String?>> = ArrayList()

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
    viewModel.restrictEventTrigger=true
    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.dmtStatus = intent.getStringExtra(RequestTypeIntentKey) ?: ""
    viewModel.fromPage = intent.getBooleanExtra(FromPage, false)
    viewModel.active = intent.getBooleanExtra(ActiveBid, false)
     source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW

    val addressDetailAdapter : AddressDetailAdapter = AddressDetailAdapter(uploadArray)
    binding.addresslist.apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = addressDetailAdapter
    }


    binding.seeMoreLay.setOnClickListener {
      if(binding.addresslist.visibility == View.VISIBLE){
        binding.addresslist.visibility = View.GONE
        binding.firstItem.visibility = View.VISIBLE
        binding.arr.rotation = 0F
      }else if(binding.addresslist.visibility == View.GONE){
        binding.addresslist.visibility = View.VISIBLE
        binding.firstItem.visibility = View.GONE
        binding.arr.rotation = 180F
      }
    }

  }

  private fun setTimer(bidEndingTime: String) {

    if(!bidEndingTime.equals("null")) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"));
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(bidEndingTime)
      if (date2.compareTo(date1) > 0) {
        binding.timerLayout.visibility = View.VISIBLE
        val mills: Long = date2.getTime() - date1.getTime()
        object : CountDownTimer(mills, 1000) {
          override fun onTick(millisUntilFinished: Long) {
            try {
              val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
              val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
              val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
              val format = "%1$02d"
              val hrs = String.format(format, hours)
              val ms = String.format(format, mins)
              val sec = String.format(format, secs)
              val diff = "$hrs:$ms:$sec" + "s"
              binding.timerTime.setText(diff)
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }

          override fun onFinish() {
            binding.timerLayout.visibility = View.GONE
          }
        }.start()
      } else {
        binding.timerLayout.visibility = View.GONE
      }
    }else{
      binding.timerLayout.visibility = View.GONE
    }
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
        if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) {
          for (transactionBid in it.second.bulkTransactionBids) {
            if (oldAmountbids.isNullOrEmpty()) {
              oldAmountbids = transactionBid.bidAmount.toString()
            } else {
              oldAmountbids = oldAmountbids + "," + transactionBid.bidAmount.toString()
            }
          }
          analyticsUtil.moEngageTrackEvent(
              EVENT_BID_REVISE_INITIATED,
              mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
              mutableListOf(
                  it.second.uuid.toString(),viewModel.bidCount.toString(),
                 viewModel.lowestBid.toString()
              )
          )
          reviseInitiated=true
        }else{
          analyticsUtil.moEngageTrackEvent(
              EVENT_ORDER_DETAILS_BID_INITIATE,
              mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE),
              mutableListOf(it.second.uuid.toString(), viewModel.bidCount.toString(),source)
          )
        }
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
      viewModel.refreshCalled=true
      refreshData()
    }

    viewModel.indentLiveData.observe(this, Observer {
        if(!it.isEmpty()){
          runOnUiThread {
            binding.textViaLabel.visibility = View.VISIBLE

            uploadArray.clear()

            if(binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
            }

          if(it.containsKey(1)){
           binding.textViaDestination.card1.visibility = View.VISIBLE
            binding.textViaDestination.city1.text = it.get(1)?.first
            if(it.get(1)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(1)?.second + " " + it.get(1)?.third))
            }
          }
          if(it.containsKey(2)){
            binding.textViaDestination.card2.visibility = View.VISIBLE
            binding.textViaDestination.city2.text = it.get(2)?.first
            if(it.get(2)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(2)?.second + " " + it.get(2)?.third))
            }
          }
          if(it.containsKey(3)){
            binding.textViaDestination.card3.visibility = View.VISIBLE
            binding.textViaDestination.city3.text = it.get(3)?.first
            if(it.get(3)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(3)?.second + " " + it.get(3)?.third))
            }
          }
          if(it.containsKey(4)){
            binding.textViaDestination.card4.visibility = View.VISIBLE
            binding.textViaDestination.city4.text = it.get(4)?.first
            if(it.get(4)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(4)?.second + " " + it.get(4)?.third))
            }
          }
          if(it.containsKey(5)){
            binding.textViaDestination.card5.visibility = View.VISIBLE
            binding.textViaDestination.city5.text = it.get(5)?.first
            if(it.get(5)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(5)?.second + " " + it.get(5)?.third))
            }
          }
          if(it.containsKey(6)){
            binding.textViaDestination.card6.visibility = View.VISIBLE
            binding.textViaDestination.city6.text = it.get(6)?.first
            if(it.get(6)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(6)?.second + " " + it.get(6)?.third))
            }
          }
          if(it.containsKey(7)){
            binding.textViaDestination.card7.visibility = View.VISIBLE
            binding.textViaDestination.city7.text = it.get(7)?.first
            if(it.get(7)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(7)?.second + " " + it.get(7)?.third))
            }
          }
          if(it.containsKey(8)){
            binding.textViaDestination.card8.visibility = View.VISIBLE
            binding.textViaDestination.city8.text = it.get(8)?.first
            if(it.get(8)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(8)?.second + " " + it.get(8)?.third))
            }
          }
          if(it.containsKey(9)){
            binding.textViaDestination.card9.visibility = View.VISIBLE
            binding.textViaDestination.city9.text = it.get(9)?.first
            if(it.get(9)?.second.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", it.get(9)?.second + " " + it.get(9)?.third))
            }
          }
            if(binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Drop Address", binding.transaction?.dropLocationAddress))
            }

            if(!uploadArray.isEmpty()) {
              binding.addressLay.visibility = View.VISIBLE
              val addressDetailAdapter = AddressDetailAdapter(uploadArray)
              binding.addresslist.apply {
                layoutManager = LinearLayoutManager(applicationContext)
                adapter = addressDetailAdapter
              }
            }else{
              binding.addressLay.visibility = View.GONE
            }

          }
      }
    })

    refreshData()
  }

  private fun refreshData() {
    binding.error = false
    viewModel.fetchTransactionDetails()
    binding.executePendingBindings()
    uploadArray.clear()
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


        binding.head.text = "Pickup Address"
        binding.subHead.text = binding.transaction?.pickupLocationAddress

        if(binding.transaction?.indentOrigin.equals("LH")){
          if(binding.transaction?.indentHaltCenters.isNullOrEmpty()){
            binding.stopNo.text = "No Stops"
          }else{
            binding.stopNo.text = binding.transaction?.indentHaltCenters!!.size.toString()+" Stops"
            for (i in binding.transaction?.indentHaltCenters!!.indices) {
              viewModel.fetchIndentCenters(binding.transaction?.indentHaltCenters!![i].haltCenterCode, i+1)
            }
          }
        }else{
          var total = 0

          if(binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup1City)) {
            total = total+1
            binding.textViaDestination.card1.visibility = View.VISIBLE
            binding.textViaDestination.city1.text = binding.transaction?.pickup1City
            if(binding.transaction?.pickup1Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", binding.transaction?.pickup1Address))
            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup2City)) {
            total = total+1
            binding.textViaDestination.card2.visibility = View.VISIBLE
            binding.textViaDestination.city2.text = binding.transaction?.pickup2City
            if(binding.transaction?.pickup2Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", binding.transaction?.pickup2Address))
            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.stop1City)) {
            if(total==0){
              binding.textViaDestination.img3.visibility = View.GONE
            }
            total = total+1
            binding.textViaDestination.card3.visibility = View.VISIBLE
            binding.textViaDestination.city3.text = binding.transaction?.stop1City
            if(binding.transaction?.intermediaryStop1Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Drop Intermediary Stop", binding.transaction?.intermediaryStop1Address))
            }
          }
          if (!TextUtils.isEmpty(binding.transaction?.stop2City)) {
            total = total+1
            binding.textViaDestination.card4.visibility = View.VISIBLE
            binding.textViaDestination.city4.text = binding.transaction?.stop2City
            if(binding.transaction?.intermediaryStop2Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Drop Intermediary Stop", binding.transaction?.intermediaryStop2Address))
            }
          }

          if(total>0){
            binding.textViaLabel.visibility = View.VISIBLE
            binding.stopNo.text = "$total Stops"
          }else{
            binding.stopNo.text = "No Stops"
          }

          if(binding.transaction?.dropLocationAddress.isNotNullOrEmpty()){
            uploadArray.add(Pair("Drop Address",binding.transaction?.dropLocationAddress))
          }

          if(!uploadArray.isEmpty()) {
            binding.addressLay.visibility = View.VISIBLE
            val addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.addresslist.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = addressDetailAdapter
            }
          }else{
            binding.addressLay.visibility = View.GONE
          }
        }


        bidEndingTime = binding.transaction!!.bidEndingTime.toString()

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
                  if(binding.textBulkLoad.visibility == View.GONE) {
                    binding.timerLayout.visibility = View.VISIBLE
                   setTimer(bidEndingTime)
                  }
                  isFirstBid = true
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE),
                    mutableListOf(viewModel.transactionId,source )
                  )
                  btnPlaceBid.setOnClickListener { bidDialog() }
               }
          }
          is BidDetailsUserBidState_PlaceBid -> {
            ViewBidDetailsPlaceBidBinding.inflate(layoutInflater, binding.containerActions, false)
              .apply {
                if(binding.textBulkLoad.visibility == View.GONE) {
                  binding.timerLayout.visibility = View.VISIBLE
                 setTimer(bidEndingTime)
                }
                isFirstBid = true
                bidsRecieved = state.bidsCount
                state.lowestAndUserBidPair.second?.let {
                  lowestBid = when (state.lowestAndUserBidPair) {
                    null -> ""
                    else -> "Lowest Bid - ₹ ${StringUtils.formatAmount(
                              state.lowestAndUserBidPair.second?.bidAmount ?: 0.0
                    )}" + if (state.isPMTIndent) "/MT" else ""
                  }
                }
                analyticsUtil.moEngageTrackEvent(
                  EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                  mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE),
                  mutableListOf(viewModel.transactionId,state.bidsCount.toString(), source)
                )
                btnPlaceBid.setOnClickListener { bidDialog() }
              }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
                .apply {
                  binding.timerLayout.visibility = View.GONE
                  val data = viewModel.transaction as HomeBidsRequestItemData
                  data.numBids = state.bidsCount
                  var oldAmount= data?.transactionBid?.bidAmount
                  data.transactionBid = state.lowestAndUserBidPair.first
                  bidsRecieved = state.bidsCount
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE),
                    mutableListOf(viewModel.transactionId,bidsRecieved.toString(), source)
                  )
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
                  if((!viewModel.restrictEventTrigger && !viewModel.refreshCalled)||isFirstBid) {
                    isFirstBid = false
                    if (!reviseInitiated) {
                      analyticsUtil.moEngageTrackEvent(
                          EVENT_ORDER_DETAILS_BID_SUBMIT,
                          mutableListOf(
                              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                              PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                              PROPERTY_SOURCE
                          ),
                          mutableListOf(
                              state.lowestAndUserBidPair.second?.transactionId ?: "",
                              state.bidsCount.toString(),
                              state.lowestAndUserBidPair.second?.bidAmount.toString(),
                              state.lowestAndUserBidPair.second?.expectedArrivalTimePickupRemark.toString(),
                              source
                          )
                      )
                    } else {
                      analyticsUtil.moEngageTrackEvent(
                          EVENT_BID_REVISE_SUBMITTED,
                          mutableListOf(
                              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT,
                              PROPERTY_ORDER_LOWEST_BID_VALUE,
                              PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW
                          ),
                          mutableListOf(
                              state.lowestAndUserBidPair.second?.transactionId ?: "",
                              state.bidsCount.toString() ?: "", data?.lowestBid.toString() ?: " ",
                              oldAmount.toString() ?: "", data?.bidAmountValue()
                              .toString() ?: ""
                          )
                      )
                      reviseInitiated = false
                    }
                  }else{
                    viewModel.refreshCalled=false
                  }
                  viewModel.restrictEventTrigger=false
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

                  btnEditBidInsider.setOnClickListener(View.OnClickListener {
                    analyticsUtil.moEngageTrackEvent(
                        EVENT_NOT_LOWEST_BID_CTA,
                        mutableListOf(PROPERTY_ORDER_ID),
                        mutableListOf(state.lowestAndUserBidPair.second?.transactionId ?: "")
                    )
                    bidDialog(userBid) })
                  textEditBid.setOnClickListener(View.OnClickListener {
                    analyticsUtil.moEngageTrackEvent(
                        EVENT_LOWEST_BID_CTA,
                        mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                        mutableListOf(state.lowestAndUserBidPair.second?.transactionId ?: "",
                            state.bidsCount.toString())
                    )
                    bidDialog(userBid)
                  })
                  textEditBid2.setOnClickListener(View.OnClickListener {
                    analyticsUtil.moEngageTrackEvent(
                        EVENT_LOWEST_BID_CTA,
                        mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                        mutableListOf(state.lowestAndUserBidPair.second?.transactionId ?: "",
                            state.bidsCount.toString())
                    )
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                      mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT,PROPERTY_SOURCE),
                      mutableListOf(viewModel.transactionId,state.bidsCount.toString(), source)
                    )
                    bidDialog(userBid) })
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
                    binding.timerLayout.visibility = View.GONE
                    pickUpLocation =
                            StringUtils.capitalize(state.pickupLocation)
                                    ?: getString(string.not_available)
                    vehicleNumber = state.vehicleNumber ?: getString(string.not_available)
                    driverPhone =
                            state.driverDetails?.driverPhoneNo ?: getString(string.not_available)

                    analyticsUtil.moEngageTrackEvent(
                      EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                      mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE),
                      mutableListOf(viewModel.transactionId, source)
                    )
                  }

          }
          is BidDetailsUserBidState_RejectedBid -> {
            ViewBidDetailsRejectedBidBinding.inflate(
                layoutInflater, binding.containerActions, false
            )
                  .apply {
                    binding.timerLayout.visibility = View.GONE
                    val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                      StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0) + "/MT"
                    } else {
                      StringUtils.formatAmount(state.userBid.bidAmount)
                    }
                    textUserHighestBid.text = bidText
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                      mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE),
                      mutableListOf(viewModel.transactionId, source)
                    )
                   }
          }
          is BidDetailsUserBidState_CancelledBid -> {
            ViewBidDetailsCancelledBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.bidAmount ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                analyticsUtil.moEngageTrackEvent(
                  EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                  mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE),
                  mutableListOf(viewModel.transactionId, source)
                )
                textUserHighestBid.text = bidText
              }
          }

          is BidDetailsUserBidState_BulkLoad_Edit -> {
            ViewBidDetailsBulkLoadEditBinding.inflate(
                    layoutInflater, binding.containerActions, false
            ).apply {
              binding.timerLayout.visibility = View.GONE
              val data = viewModel.transaction as HomeBidsRequestItemData
              var bidAmount =""
              var expectedArrivalPickup=""
              if (data.transactionStatus == "cancelled") {
                btnReviseBidInsider.visibility = View.GONE
                reduceText.visibility = View.GONE
              }
              data.bulkTransactionBids = state.bids
              bidsRecieved = state.bidsCount
              data.let {
                if(!it.bulkTransactionBids.isNullOrEmpty()) {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE),
                    mutableListOf(viewModel.transactionId,bidsRecieved.toString(), source)
                  )

                }else{
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE),
                    mutableListOf(viewModel.transactionId, source)
                  )
                }
              }
              data.let {
                if(!it.bulkTransactionBids.isNullOrEmpty()) {
                  for (transactionBid in it.bulkTransactionBids) {
                    if (bidAmount.isNullOrEmpty()) {
                      bidAmount = transactionBid.bidAmount.toString()
                    } else {
                      bidAmount = bidAmount + "," + transactionBid.bidAmount.toString()
                    }
                    if (expectedArrivalPickup.isNullOrEmpty()) {
                      expectedArrivalPickup =
                        transactionBid.expectedArrivalTimePickupRemark.toString()
                    } else {
                      expectedArrivalPickup =
                        expectedArrivalPickup + transactionBid.expectedArrivalTimePickupRemark.toString()
                    }
                  }
                }
              }
              rvBidSummary.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BidDetailsActivity.adapter
                (adapter as BulkBidsRVAdapter).clearItems()
                viewModel.getUserBulkBids(
                    state.bids, state.lowestAndUserBidPair.second.let { it!!.bidAmount })

              }

              btnReviseBidInsider.setOnClickListener {
                analyticsUtil.moEngageTrackEvent(
                    EVENT_NOT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID),
                    mutableListOf(data.uuid.toString())
                )
                bidDialog()
              }
              if ((!viewModel.restrictEventTrigger && !viewModel.refreshCalled)||isFirstBid) {
                isFirstBid = false
                if (!reviseInitiated) {
                  analyticsUtil.moEngageTrackEvent(
                      EVENT_ORDER_DETAILS_BID_SUBMIT,
                      mutableListOf(
                          PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                          PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                          PROPERTY_SOURCE
                      ),
                      mutableListOf(
                          state.lowestAndUserBidPair.second?.transactionId ?: "",
                          state.bidsCount.toString(),
                          bidAmount,
                          expectedArrivalPickup,
                          source
                      )
                  )
                } else {
                  analyticsUtil.moEngageTrackEvent(
                      EVENT_BID_REVISE_SUBMITTED,
                      mutableListOf(
                          PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
                          PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW
                      ),
                      mutableListOf(
                          state.lowestAndUserBidPair.second?.transactionId ?: "",
                          state.bidsCount.toString() ?: "", state?.lowestAndUserBidPair.first?.bidAmount.toString() ?: " ",
                          oldAmountbids, bidAmount
                      )
                  )
                  reviseInitiated = false
                }
              }else{
                viewModel.refreshCalled=false
              }
              viewModel.restrictEventTrigger=false
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

  override fun onBackPressed() {
    userPrefs.setPreviousScreen(this.javaClass.name)
    super.onBackPressed()
  }

  /**
   * Create/edit bid dialog
   */
  private fun bidDialog(bid: TransactionBid? = null) {
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        binding.transaction?.let {
          if(viewModel.dmtStatus == "dmt" || it.isDMTIndent()) {
            uiUtils.showProgress()
            viewModel.fetchTruckType(it);
          }
          else{
            if(it.transactionBid==null){
              analyticsUtil.moEngageTrackEvent(
                  EVENT_ORDER_DETAILS_BID_INITIATE,
                  mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE),
                  mutableListOf(it?.uuid ?:"",viewModel.bidCount.toString(),source)
              )
              reviseInitiated=false
            }else{
              analyticsUtil.moEngageTrackEvent(
                  EVENT_BID_REVISE_INITIATED,
                  mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
                  mutableListOf(
                      it?.uuid.toString(), viewModel.bidCount.toString(),
                    viewModel.lowestBid.toString()
                  )
              )
             reviseInitiated=true
            }
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
  active:Boolean = false,
  source:String?= VALUE_APP_FLOW
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  if(requestType!=null)
  putExtra(RequestTypeIntentKey, requestType)
  putExtra(FromPage,fromBidsPage)
  putExtra(ActiveBid,active)
  putExtra(PROPERTY_SOURCE,source)
}
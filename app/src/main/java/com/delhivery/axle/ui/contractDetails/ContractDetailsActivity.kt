package com.delhivery.axle.ui.contractDetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TransactionStatus
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.home.bids.HaltCenters
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.PaymentSlabs
import com.delhivery.axle.databinding.ActivityContractDetailsBinding
import com.delhivery.axle.databinding.DialogContractsBidSuccessBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.BidDetailsContractCancelled
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_ContractResult
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_EditBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_LoadingBids
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBid
import com.delhivery.axle.ui.biddetails.BidDetailsUserBidState_PlaceBidFirst
import com.delhivery.axle.ui.home.fragments.contracts.REFRESH_ON_BACK
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.EVENT_HOME_CONTRACT_CARD_CLICK
import com.delhivery.axle.utils.PROPERTY_CONTRACT_TYPE
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_APP_FLOW
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class ContractDetailsActivity: BaseActivity<ActivityContractDetailsBinding, ContractDetailsViewModel>() {

  init {
    hasInlineProgress = true
  }
  @Inject lateinit var userPrefs: UserPrefs
  var routesArray:ArrayList<HaltCenters> = ArrayList()
  var paymentSlabsArray:ArrayList<PaymentSlabs> = ArrayList()
  var reportingCentersArray:ArrayList<HaltCenters> = ArrayList()
  var source= VALUE_APP_FLOW
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      require(
        !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.requestType = RequestType.Contract.type
    source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW

    binding.backArrow.setOnClickListener {
      onBackPressed()
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup live data observers */
    viewModel.progressLiveData.observe(this, ProgressObserver())
    viewModel.transactionLiveData.observe(this, TransactionObserver())
   viewModel.transactionBidLiveData.observe(this, TransactionBidObserver())
    viewModel.bidPriceLiveData.observe(this, Observer {
      if (it != null) {
        binding.transaction?.transactionBid = it
        val visibility =
          if (binding.transaction?.bidAmount()
              .isNullOrEmpty()
          ) View.GONE else View.VISIBLE
      }
    })

    // show Success Bid Dialog
    viewModel.showSuccessPlaceReviseDialogLiveData.observe(this, Observer {
      showSuccessPlaceReviseDialog(it)

    })
    viewModel.hideProgress.observe(this, Observer {
      if(it){
      uiUtils.hideProgress()
        binding.bottomLay.visibility = View.VISIBLE
      }
    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }
    binding.buttonConfirm.setOnClickListener {
      bidDialog()
    }

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }


    refreshData()
  }

  var countDownTimer: CountDownTimer? = null
  // timer for under 1 hrs slot
 fun setTimer(bidEndingTime:String){

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(bidEndingTime)
        if (date2.compareTo(date1) > 0) {
          val mills: Long = date2.getTime() - date1.getTime()
          countDownTimer?.cancel()
          countDownTimer = object : CountDownTimer(mills, 1000) {
            override fun onTick(millisUntilFinished: Long) {
              try {
                val hours = (millisUntilFinished / (1000 * 60 * 60)).toInt()
                val mins = (millisUntilFinished / (1000 * 60)).toInt() % 60
                val secs = ((millisUntilFinished / 1000).toInt() % 60).toLong()
                val format = "%1$02d"
                val hrs = String.format(format, hours)
                val ms = String.format(format, mins)
                val sec = String.format(format, secs)
                val diff = ms + "m $sec" + "s"
                if (hours == 0) {
                  binding.tvBidTime.visibility = View.VISIBLE
                  binding.tvBidTime.setText("Closes in $diff")
                  binding.bidClosingStatusTime.visibility = View.VISIBLE
                  binding.bidClosingStatusTime.text = diff
                  binding.bidClosingStatusTime.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                }else  if(hours<=12){
                  binding.bidClosingStatusTime.visibility = View.VISIBLE
                  binding.bidClosingStatusTime.text = hrs+ " hrs"
                  binding.bidClosingStatusTime.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.heading_black))
                }else{
                  if(viewModel.transaction.transactionStatus==TransactionStatus.Cancelled.statusId){
                    binding.bidClosingStatusTime.visibility = View.VISIBLE
                  }else{
                    binding.bidClosingStatusTime.visibility = View.GONE
                    binding.bidClosingStatus.text = viewModel.transaction.bidEndTime()
                  }

                }

              } catch (e: Exception) {
                e.printStackTrace()
              }
            }

            override fun onFinish() {
              binding.tvBidTime.visibility = View.GONE
              binding.bottomLay.visibility = View.GONE
              refreshData()
            }
          }.start()
        } else {
          binding.tvBidTime.visibility = View.GONE
          binding.bottomLay.visibility = View.GONE
        }

 }

  private fun bidDialog(bid: TransactionBid? = null) {
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        binding.transaction?.let {
            ContractDetailsCreateEditDialog(
              this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs,
               source= source
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
          binding.routeDetails.nonExpRouteDetails.visibility = t.isFRCContract()
          binding.routeDetails.routeDetails.visibility =  t.isLHContract()
          binding.routeDetails.intraCityRouteDetails.visibility =  t.isIntraCityContract()

          binding.vehicleDetails.visibility = View.VISIBLE

          // Capture event
          analyticsUtil.moEngageTrackEvent(
            EVENT_HOME_CONTRACT_CARD_CLICK,
            mutableListOf(
              PROPERTY_USER_ID,
              PROPERTY_PHONE_NO, PROPERTY_ORDER_ID, PROPERTY_STATUS, PROPERTY_CONTRACT_TYPE,
              PROPERTY_SOURCE
            ),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",
              _transaction.uuid ?: " ",_transaction.contractEventStatusText(),
              _transaction.contractType?:"",source
            )
          )
          // for FRC contract
          if(t.isItFRContract()){
            if(t.transactionStatus==TransactionStatus.Cancelled.statusId){
              binding.routeDetails.nonExpHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.nonExpHubIcon2.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.clNonExpTimeInterval1.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_round_corner_light_grey)
              binding.routeDetails.nonExpTimeInterval1.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.heading_black))
            }else{
              binding.routeDetails.nonExpHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.nonExpHubIcon2.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.clNonExpTimeInterval1.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_round_corner_light_blue)
              binding.routeDetails.nonExpTimeInterval1.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.dark_blue))
            }
            binding.routeDetails.nonExpTvHubCity.text = t.clientName()
            binding.routeDetails.nonExpTvCity.text =  t.originCityName()
            binding.routeDetails.nonExpTvState.text =  ", "+t.originState
            binding.routeDetails.nonExpTvHubCity2.text = t.destinationCityName()
            binding.routeDetails.nonExpTvState2.text = t.destinationState
            binding.routeDetails.nonExpTimeInterval1.text  =
              t.tentativeTripCount?.let { Integer.toString(it) } +" Trips in "+ t.contractValidity+" weeks"
          }else if(t.isItIntraCityContract()){
            if(t.transactionStatus== TransactionStatus.Cancelled.statusId){
              binding.routeDetails.intraCityHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_black_hub))
              binding.routeDetails.intraCityReportingIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_time_grey))
            }else{
              binding.routeDetails.intraCityHubIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_hub_route))
              binding.routeDetails.intraCityReportingIcon.setImageDrawable(ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.ic_time))
            }
            binding.routeDetails.intraCityReportingTime.text = DateUtils.getFormattedTimeIn12Hrs(t.reportingTime?:"")
            binding.routeDetails.intraCityTvState.text = t.originState
            binding.routeDetails.intraCityTvCity.text = t.originCity?.capitalize()
            binding.routeDetails.intraCityTvHubCity.text = t.origin
            binding.routeDetails.intraCityTvMapView.setOnClickListener{
              try {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${t.latitude},${t.longitude}"+"(" + t.origin+ ")")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
              } catch (e: Exception) {
                Toast.makeText(this@ContractDetailsActivity, "Unable to open map", Toast.LENGTH_SHORT).show()
              }
            }
          }
          binding.contractRules.nonExpRules.visibility = t.isFRCContract()
          binding.contractRules.rules.visibility = t.isLHContract()
          binding.contractRules.intraCityRules.visibility = t.isIntraCityContract()
          binding.tripDetails.visibility = t.isLHContract()
          // For LH contract listing recycleview with halt centers
          if(_transaction.haltCenters!=null) {
            for (item in _transaction.haltCenters!!) {
             routesArray.add(item)
            }
            val contractsRouteDetailsAdapter  = ContractsRouteDetailsAdapter(routesArray,_transaction,this@ContractDetailsActivity)
            binding.routeDetails.rvContracts.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = contractsRouteDetailsAdapter
            }
          }
          binding.seperator.visibility = View.VISIBLE
        }
        binding.executePendingBindings()
      } else {
        binding.error = true
        binding.containerError.title = "Session Time Out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
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
          // Transaction Cancel
          is BidDetailsContractCancelled ->{
            binding.bottomLay.visibility = View.VISIBLE
            binding.confirmedText.text = getString(string.place_your_bid)
            binding.confirmedText.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.color_hint))
            binding.clBidYet.visibility = View.GONE
            binding.biddingIcon.setImageDrawable( ContextCompat.getDrawable(
              this@ContractDetailsActivity,
              R.drawable.ic_cancel_icon
            ))
            binding.bidClosingStatus.visibility = View.VISIBLE
            binding.bidClosingStatus.text = getString(string.bidding_cancelled)
            binding.confirmedText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_money_grey, 0, 0, 0)
            binding.buttonConfirm.background = ContextCompat.getDrawable(
              this@ContractDetailsActivity,
              R.drawable.bg_all_round_corner_light_grey
            )
            binding.buttonConfirm.isClickable = false
            binding.clBidStatus.visibility = View.VISIBLE
            binding.tvBidStatus.text = getString(string.cancelled)
            binding.tvBidTime.visibility = View.GONE
            binding.tvBidStatus.setTextColor(
               ContextCompat.getColor(
                 this@ContractDetailsActivity,
                 R.color.heading_black
               )
             )
            binding.statusImage.setImageDrawable(
               ContextCompat.getDrawable(
                 this@ContractDetailsActivity,
                 R.drawable.ic_cancel_contract
               )
             )
            binding.clBidStatus.background = ContextCompat.getDrawable(
               this@ContractDetailsActivity,
               R.drawable.bg_all_round_corner_light_grey
             )

           }
          //Placed first bid
          is BidDetailsUserBidState_PlaceBidFirst -> {
            binding.bottomLay.visibility = View.VISIBLE
            binding.confirmedText.text = getString(string.place_your_bid)
            binding.clBidYet.visibility = View.VISIBLE
            binding.buttonConfirm.setOnClickListener {
              bidDialog()
            }
            setBidStatusHeaderLayout(viewModel.transaction)
          }
          //Placed your first bid
          is BidDetailsUserBidState_PlaceBid -> {
            binding.bottomLay.visibility = View.VISIBLE
            binding.confirmedText.text = getString(string.place_your_bid)
            binding.clBidYet.visibility = View.VISIBLE
            binding.bidPmtFtl.visibility = View.GONE
            binding.buttonConfirm.setOnClickListener {
              bidDialog()
            }
            setBidStatusHeaderLayout(viewModel.transaction)
          }
          //Edit bid
          is BidDetailsUserBidState_EditBid -> {
           binding.bottomLay.visibility = View.VISIBLE
            binding.confirmedText.text = getString(string.revise_your_bid)
            val data = viewModel.transaction
            data.numBids = state.bidsCount
            data.transactionBid = state.lowestAndUserBidPair.first
            val userBid = state.lowestAndUserBidPair.first
            val lowestTBid = state.lowestAndUserBidPair.second
            data.lowestBid = lowestTBid?.bidAmount

            // For intracity payment slabs if user has bids
              showPayoutSlabs(data)
            //for more than 1 bids and within live bidding
            if(state.bidsCount>1 && data.isUnderOneHour()){
              binding.clBidYet.visibility = View.GONE
              binding.clExpressBidStatus.visibility = data.isLHIntraCityContract()
              binding.clNonExpressBidStatus.visibility = data.isFRCContract()
              //LH contract within live bidding
              if(data.isItLHContract() || data.isItIntraCityContract()){
                binding.bidPmtFtlStatus.visibility = View.VISIBLE
                binding.bidReceived.text = state.bidsCount.toString()+" "+ getString(string.bid_received)
                binding.bidAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                if (lowestTBid?.bidAmount != null) {
                  // user bid same as Lowest bid
                  if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                    binding.bidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.bid_placed_green))
                    binding.bidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_won)
                    binding.bidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_low_bid, 0, 0, 0)
                    binding.bidStatus.text = " Your bid is the lowest"
                  }else{
                    binding.bidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                    binding.bidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_lost_red)
                    binding.bidStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    binding.bidStatus.text = getString(string.higher_than)+" "+data.contractLowestbidDifference()
                  }
                }
              }else{
                // FRC contract within live bidding
                binding.tripCommitted.text = userBid?.tentativeTripCount.toString()
                binding.nonExpressBidReceived.text = state.bidsCount.toString()+ " Bids Received"
                binding.nonExpressBidAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                binding.nonExpressBidPmtFtlStatus.visibility = data.isFRCContract()
                if(data.biddingType?.toLowerCase()=="pmt"){
                       binding.nonExpressBidPmtFtlStatus.text = getString(string.your_pmt_rate)
                        }else{
                       binding.nonExpressBidPmtFtlStatus.text = getString(string.your_ftl_rate)
                        }
                // user bid same as Lowest bid
                if (lowestTBid?.bidAmount != null) {
                  if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true ) {
                    binding.nonExpressBidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.bid_placed_green))
                    binding.nonExpressBidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_won)
                    binding.nonExpressBidStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_low_bid, 0, 0, 0)
                    binding.nonExpressBidStatus.text = " Your bid is the lowest"
                  }else{
                    binding.nonExpressBidStatus.setTextColor(ContextCompat.getColor(this@ContractDetailsActivity,R.color.destructive_red))
                    binding.nonExpressBidStatus.background = ContextCompat.getDrawable(this@ContractDetailsActivity,R.drawable.bg_all_rounded_lost_red)
                    binding.nonExpressBidStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    binding.nonExpressBidStatus.text = getString(string.higher_than)+" "+data.contractLowestbidDifference()
                  }
                }
              }
            }else{
                    // if only 1 bid -> no target price or bid amount < Lowest bid
                    binding.clExpressBidStatus.visibility = View.GONE
                    binding.clNonExpressBidStatus.visibility =View.GONE
                    binding.clBidYet.visibility = View.VISIBLE
                    binding.bidUserStatusOrAmount.text = "₹ "+StringUtils.formatAmount(userBid?.bidAmount!!)
                    binding.bidPmtFtl.visibility = View.VISIBLE
                    if(data.isItLHContract()|| data.isItIntraCityContract()){
                      binding.bidPmtFtl.text =getString(string.your_bid)
                    }else{
                      if(data.biddingType?.toLowerCase()=="pmt"){
                        binding.bidPmtFtl.text = getString(string.your_pmt_rate)
                      }else{
                        binding.bidPmtFtl.text = getString(string.your_ftl_rate)
                      }
                    }



            }
            binding.buttonConfirm.setOnClickListener {
              bidDialog(userBid)
            }
            setBidStatusHeaderLayout(data)
          }
          is BidDetailsUserBidState_LoadingBids -> {
            binding.bottomLay.visibility = View.GONE
            uiUtils.showProgress()
          }
          //Result for closed bidding time
          is BidDetailsUserBidState_ContractResult -> {

            val data = viewModel.transaction
            data.transactionBid =  state.lowestAndUserBidPair.first
            data.numBids = state.bidsCount
            val userBid = state.lowestAndUserBidPair.first
            val lowestTBid = state.lowestAndUserBidPair.second
             data.lowestBid =lowestTBid?.bidAmount
             showPayoutSlabs(data)
            binding.bottomLay.visibility = View.GONE
            binding.viewMarginResult.visibility = View.VISIBLE

            binding.viewMarginRoute.visibility = View.VISIBLE
            binding.viewMarginRule.visibility = View.VISIBLE

            binding.contractResult.bidAmountLayout.visibility = View.VISIBLE
            binding.contractResult.seperator.visibility = data.isFRCContract()
            binding.contractResult.yourTripCommitment.visibility =data.isFRCContract()
            binding.contractResult.yourTripCommitmentValue.visibility =data.isFRCContract()
            binding.contractRulesWithResult.rules.visibility = data.isLHContract()
            binding.contractRulesWithResult.intraCityRules.visibility = data.isIntraCityContract()
            binding.contractRulesWithResult.nonExpRules.visibility = data.isFRCContract()
            binding.contractRules.rules.visibility = View.GONE
            binding.contractRules.nonExpRules.visibility = View.GONE
            binding.contractRules.intraCityRules.visibility = View.GONE
            if(data.isItFRContract()){
              binding.contractResult.yourTripCommitmentValue.text = data.transactionBid?.tentativeTripCount?.toString()
            }
            binding.viewMarginWithoutResult.visibility = View.GONE
              if(userBid?.bidAmount==null){
              binding.contractResult.haveNoBid.visibility = View.VISIBLE
                binding.contractResult.contractBidResultLayout.visibility = View.GONE
            }else{
              binding.contractResult.haveNoBid.visibility = View.GONE
                binding.contractResult.contractBidResultLayout.visibility = View.VISIBLE
            }
            if(userBid?.status()==Open){
              binding.contractResult.resultDescribe.visibility = View.VISIBLE
              binding.contractResult.yourBidAmountValue.text = "₹"+StringUtils.formatAmount(userBid?.bidAmount!!)
            }else if(userBid?.status()==Accepted){
              binding.contractResult.resultDescribe.visibility = View.VISIBLE
              binding.contractResult.resultDescribe.text = getString(string.won_contracts)
              binding.contractResult.yourBidAmountValue.text = "₹"+StringUtils.formatAmount(userBid?.bidAmount!!)
            }else{
              if(userBid?.bidAmount!=null) {
                binding.contractResult.resultDescribe.visibility = View.VISIBLE
                binding.contractResult.yourBidAmountValue.text =
                  "₹" + StringUtils.formatAmount(userBid?.bidAmount!!)
                if (lowestTBid?.bidAmount != null) {
                  if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                    binding.contractResult.resultDescribe.text =
                      getString(string.not_meeting_expectation)
                  } else {
                    if (data.userBidLessThanFivePercentage()) {
                      binding.contractResult.resultDescribe.text =
                        getString(string.less_than_5_percentage)
                    } else {
                      binding.contractResult.resultDescribe.text =
                        getString(string.more_than_5_percentage)

                    }

                  }
                } else {
                  binding.contractResult.resultDescribe.visibility = View.GONE
                }
              }
            }
            if(userBid?.status()==Open) {
              binding.clBidStatus.visibility = View.VISIBLE
              binding.tvBidTime.text = getString(string.awaiting_results)
              binding.tvBidStatus.text=getString(string.bidding_closed)
              binding.tvBidTime.visibility = View.VISIBLE
              binding.tvBidStatus.setTextColor(
                ContextCompat.getColor(
                  this@ContractDetailsActivity,
                  R.color.dark_blue
                )
              )
              binding.statusImage.setImageDrawable(
                ContextCompat.getDrawable(
                  this@ContractDetailsActivity,
                  R.drawable.ic_awaiting_bid
                )
              )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.bg_all_rounded_under_review
              )
            }else{

              binding.clBidStatus.visibility = View.VISIBLE
              binding.tvBidStatus.text = getString(string.result_declared)
              binding.tvBidTime.visibility = View.GONE
              binding.tvBidStatus.setTextColor(
                ContextCompat.getColor(
                  this@ContractDetailsActivity,
                  R.color.bid_placed_green
                )
              )
              binding.statusImage.setImageDrawable(
                ContextCompat.getDrawable(
                  this@ContractDetailsActivity,
                  R.drawable.ic_bid_result
                )
              )
              binding.clBidStatus.background = ContextCompat.getDrawable(
                this@ContractDetailsActivity,
                R.drawable.bg_all_rounded_won
              )
            }
          }

          else -> null
        }
        }
      }
    }

  private fun showPayoutSlabs(data:HomeBidsRequestItemData) {
    if(data.paymentSlabs!=null && data.isItIntraCityContract()&& data.transactionStatus!=TransactionStatus.Cancelled.statusId) {
      val keys = data.paymentSlabs!!.keySet()
      binding.transaction = data
      paymentSlabsArray = ArrayList()
      paymentSlabsArray.add(PaymentSlabs("Kms","Monthly Payout"))
      for (key in keys) {
        val value = data.paymentSlabs!!.get(key).asString
        paymentSlabsArray.add(PaymentSlabs(key,"₹ "+value))
      }
        binding.paymentSlabsMoreSlab.visibility = if(data.isPaymentSlabsVisible()&&keys.size>3) View.VISIBLE else View.GONE

      val contractPaymentSlabsAdapter  = ContractPaymentSlabsAdapter(paymentSlabsArray,data,this@ContractDetailsActivity)
      binding.rvPaymentSlabs.apply {
        layoutManager = LinearLayoutManager(applicationContext)
        adapter = contractPaymentSlabsAdapter
      }
      var isExpanded = false
      binding.paymentSlabsMoreSlab.text = getString(string.see_full_slabs_list)
      binding.paymentSlabsMoreSlab.setOnClickListener {
        isExpanded = !isExpanded
        contractPaymentSlabsAdapter.expand(isExpanded)
        if(isExpanded){
          binding.paymentSlabsMoreSlab.text = getString(string.hide_slabs)
        }else{
          binding.paymentSlabsMoreSlab.text = getString(string.see_full_slabs_list)
        }

      }
    }
  }

  // set header status and bottom button
   fun setBidStatusHeaderLayout(data:HomeBidsRequestItemData) {
     if (data.isContractBiddingOpen()) {
     if (data.isUnderOneHour()) {
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidStatus.text = getString(string.live_bidding)
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.destructive_red
         )
       )
       GlideApp.with(this@ContractDetailsActivity)
         .load(R.raw.livebidding)
         .apply(RequestOptions().override(40, 40))
         .into(binding.statusImage)

       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_lost_red
       )
     } else {
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidStatus.text = getString(string.collecting_bids)
       binding.tvBidTime.text = data.bidEndDate()

       binding.tvBidTime.visibility = View.VISIBLE
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.pending_status
         )
       )
       binding.statusImage.setImageDrawable(
         ContextCompat.getDrawable(
           this@ContractDetailsActivity,
           R.drawable.ic_money_pending
         )
       )
       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_pending
       )
     }
       setTimer(data.contractBiddingEndTime!!)

   }else{
       binding.clBidStatus.visibility = View.VISIBLE
       binding.tvBidTime.text = getString(string.awaiting_results)
       binding.tvBidStatus.text=getString(string.bidding_closed)
       binding.tvBidTime.visibility = View.VISIBLE
       binding.tvBidStatus.setTextColor(
         ContextCompat.getColor(
           this@ContractDetailsActivity,
           R.color.dark_blue
         )
       )
       binding.statusImage.setImageDrawable(
         ContextCompat.getDrawable(
           this@ContractDetailsActivity,
           R.drawable.ic_awaiting_bid
         )
       )
       binding.clBidStatus.background = ContextCompat.getDrawable(
         this@ContractDetailsActivity,
         R.drawable.bg_all_rounded_under_review
       )
            binding.bottomLay.visibility = View.GONE
            binding.viewMarginResult.visibility = View.VISIBLE
            binding.contractResult.haveNoBid.visibility = View.VISIBLE
            binding.viewMarginRoute.visibility = View.VISIBLE
            binding.viewMarginRule.visibility = View.VISIBLE
            binding.contractResult.contractBidResultLayout.visibility = View.GONE
            binding.contractResult.bidAmountLayout.visibility = View.GONE
            binding.contractResult.seperator.visibility = View.GONE
            binding.contractResult.yourTripCommitment.visibility = View.GONE
            binding.contractResult.yourTripCommitmentValue.visibility =View.GONE
            binding.contractRulesWithResult.rules.visibility = data.isLHContract()
            binding.contractRulesWithResult.nonExpRules.visibility = data.isFRCContract()
            binding.contractRulesWithResult.intraCityRules.visibility = data.isIntraCityContract()
            binding.contractRules.rules.visibility = View.GONE
            binding.contractRules.nonExpRules.visibility = View.GONE
            binding.contractRules.intraCityRules.visibility = View.GONE
            if(data.isItFRContract()){
              binding.contractResult.yourTripCommitmentValue.text = data.transactionBid?.tentativeTripCount?.toString()
            }
            binding.viewMarginWithoutResult.visibility = View.GONE
    }
   }
  override fun getViewModelClass()= ContractDetailsViewModel::class.java

  override fun layoutId(): Int= R.layout.activity_contract_details
  override fun requireConnection(): Boolean = true

  private fun refreshData() {
    binding.error = false
    viewModel.fetchTransactionDetails()
    binding.executePendingBindings()
    routesArray.clear()
  }

  private fun showSuccessPlaceReviseDialog(bidInfo:Triple<Pair<String,String>,String?,Pair<Boolean,Boolean>>){
       REFRESH_ON_BACK = true
      val dialog = Dialog(this)
      val bindingDialog = DialogContractsBidSuccessBinding.inflate(this.layoutInflater)
      bindingDialog.transaction = viewModel.transaction
      bindingDialog.buttonCancel.setOnClickListener {
        dialog.cancel()
        uiUtils.hideProgress()
         refreshData()
      }
       dialog.setCancelable(false)
      if(bidInfo.third.second){
        bindingDialog.title.text = getString(string.bid_revise_successfully)
      }else{
        bindingDialog.title.text = getString(string.bid_placed_sucessfully)
      }
       if(viewModel.transaction.isItLHContract()){
         bindingDialog.yourBidAmountValue.text = "₹ "+bidInfo.first.first
       }else if(viewModel.transaction.isItFRContract()){
         bindingDialog.yourTripValue.text = bidInfo.second
         if(bidInfo.third.first){
         bindingDialog.yourBidAmountValue.text = "₹ "+bidInfo.first.second+ "\n (PMT)"
         }
         else{
           bindingDialog.yourBidAmountValue.text= "₹ "+bidInfo.first.first+ "\n (FTL)"
         }
       }else if(viewModel.transaction.isItIntraCityContract()){
         bindingDialog.yourVehicleValue.text = bidInfo.second
         bindingDialog.yourBidAmountValue.text= "₹ "+bidInfo.first.first
       }

      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setContentView(bindingDialog.root)
      dialog.show()
      dialog.window!!.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
  }
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/* intent keys */
private const val RequestTypeIntentKey = "request_type"
/**
 * Bid details intent
 */
fun contractDetailsIntent(
  transactionId: String,
  context: Context,
  source: String?= VALUE_APP_FLOW,
) = Intent(context, ContractDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  putExtra(PROPERTY_SOURCE,source)

}
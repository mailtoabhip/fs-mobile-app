package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

  var bidEndingTime: String = ""
  var source: String = VALUE_APP_FLOW
  var subSource: String = "NA"
  var reviseInitiated: Boolean = false
  var oldAmountbids = ""
  var isFirstBid = false
  var buttonVisible = false
  var bottomLayVisible = false
  private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }
  var uploadArray: ArrayList<Pair<String, String?>> = ArrayList()
  var stopArray: ArrayList<String> = ArrayList()
  var ellp = true

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
    viewModel.restrictEventTrigger = true
    /* set transaction id */
    viewModel.transactionId = intent.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.dmtStatus = intent.getStringExtra(RequestTypeIntentKey) ?: ""
    viewModel.fromPage = intent.getBooleanExtra(FromPage, false)
    viewModel.active = intent.getBooleanExtra(ActiveBid, false)
    source = intent.getStringExtra(PROPERTY_SOURCE) ?: VALUE_APP_FLOW
    subSource = intent.getStringExtra(PROPERTY_SUB_SOURCE) ?: "NA"

    val addressDetailAdapter: AddressDetailAdapter = AddressDetailAdapter(uploadArray)
    binding.addresslist.apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = addressDetailAdapter
    }

    binding.layRem.setOnClickListener {
      runOnUiThread {
        val l: Layout = binding.remarks.layout
        if (l != null) {
          val lines = l.lineCount
          if (lines > 0) if (l.getEllipsisCount(lines - 1) > 0 || !ellp) {
            if (ellp) {
              binding.remarks.ellipsize = null
              binding.remarks.setMaxLines(Integer.MAX_VALUE);
              ellp = false
              binding.clickRemark.rotation = 180F
            } else if (!ellp) {
              binding.remarks.setSingleLine(false)
              binding.remarks.setEllipsize(TextUtils.TruncateAt.END)
              val n = 2;
              binding.remarks.setLines(n);
              ellp = true
              binding.clickRemark.rotation = 0F
            }
          }
        }
      }
    }
  }

  private fun setTimer(bidEndingTime: String) {

    if (!bidEndingTime.equals("null")) {
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
    } else {
      binding.timerLayout.visibility = View.GONE
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = "Order ID - " + viewModel.transactionId
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
        binding.priceLay.visibility = visibility
        binding.textTargetPrice.visibility = visibility
        binding.textTargetPriceLabel.visibility = visibility
        if (visibility == View.VISIBLE) {
          binding.priceLay.visibility = View.VISIBLE
          if (binding.transaction?.isPMTIndent() == true) {
            binding.textTargetPrice.text = binding.transaction?.bidAmount() + "/MT"
          } else {
            binding.textTargetPrice.text = binding.transaction?.bidAmount()

          }
          binding.textTargetPriceLabel.text = binding.transaction?.amountLabel()
        } else {
          binding.priceLay.visibility = View.GONE
        }
      }
    })
     viewModel.reviseBidLiveData.observe(this, Observer {
          if (it.first) {
           bidDialog(it.second)
          }
        })
    viewModel.userBidsData.observe(this, Observer {
      if (it != null) {
        adapter.operation(it)
      }
    })
    viewModel.statusConfirmationPending.observe(this, Observer {
      if (it) {
        binding.status.visibility = View.VISIBLE
        binding.status.text = resources.getString(R.string.label_pending)
        binding.status.setBackgroundColor(resources.getColor(R.color.pending_bg))
        binding.status.setTextColor(resources.getColor(R.color.pending_status))
      }
    })
    viewModel.truckGetLiveData.observe(this, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        val pageTitle =
          if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
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
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_SOURCE,
              PROPERTY_SUB_SOURCE
            ),
            mutableListOf(
              it.second.uuid.toString(), viewModel.bidCount.toString(),
              viewModel.lowestBid.toString(), source, subSource
            )
          )
          reviseInitiated = true
        } else {
          analyticsUtil.moEngageTrackEvent(
            EVENT_ORDER_DETAILS_BID_INITIATE,
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
              PROPERTY_SUB_SOURCE
            ),
            mutableListOf(
              it.second.uuid.toString(),
              viewModel.bidCount.toString(),
              source,
              subSource
            )
          )
        }
        if (it.second.truckUUID != null) {
          BulkBidDetailsCreateEditDialog(
            this@BidDetailsActivity, it.second, it.second.bulkTransactionBids, it.first,
            viewModel, it.second.unAllocatedVolume!!, analyticsUtil = analyticsUtil,
            userPrefs = userPrefs, fromPage = "load_detail", pageTitle = pageTitle
          ).show()
        } else {
          Toast.makeText(this, "No Vehicle Types Found", Toast.LENGTH_SHORT)
            .show()

        }
      }
    })

    viewModel.editBulkLiveData.observe(this, Observer {
      if (it.first == 10) {
        Toast.makeText(this, "Bids Created Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (it.first == 20) {
        viewModel.refreshCalled = true
        refreshData()
        Toast.makeText(this, "Bids Updated Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (it.first == 30) {
        viewModel.refreshCalled = true
        refreshData()
        Toast.makeText(this, "Bids Deleted Successfully", Toast.LENGTH_SHORT)
          .show()
      }
      if (viewModel.editFlg[0] && viewModel.editFlg[1] && viewModel.editFlg[2]) {
        viewModel.fetchTransactionBids()
        viewModel.editFlg = mutableListOf(false, false, false)
      }
    })

    binding.containerError.btnAction.setOnClickListener {
      refreshData()
    }

    binding.refreshLayout.setOnRefreshListener {
      viewModel.refreshCalled = true
      refreshData()
    }

    viewModel.indentLiveData.observe(this, Observer {
      if (!it.isEmpty()) {
        runOnUiThread {
          binding.textViaLabel.visibility = View.VISIBLE

          uploadArray.clear()

          if (binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
          } else if (binding.transaction?.loadingLocationPincode.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Pickup Address",
                binding.transaction?.loadingLocationPincode.toString() + "-" + binding.transaction?.pickupLocationCity
              )
            )
          }

          val sortedList = it.sortedWith(compareBy({ it.first.first }))

          val stopAdapter = StopAdapter(sortedList)
          binding.stopList.apply {
            layoutManager = LinearLayoutManager(
              applicationContext, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = stopAdapter
          }

          val j = 0
          for (i in sortedList) {
            j + 1
            uploadArray.add(Pair("Pickup Intermediary Stop", i.second + " " + i.third))
          }

          if (binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Drop Address",
                binding.transaction?.dropLocationAddress.toString()
              )
            )
          } else if (binding.transaction?.unloadingLocationPincode.isNotNullOrEmpty()) {
            uploadArray.add(
              Pair(
                "Drop Address",
                binding.transaction?.unloadingLocationPincode.toString() + "-" + binding.transaction?.dropLocationCity.toString()
              )
            )
          }

          if (!uploadArray.isEmpty()) {
            binding.addressLay.visibility = View.VISIBLE
            val addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.addresslist.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = addressDetailAdapter
            }
          } else {
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
    if (binding.buttonConfirm.visibility == View.VISIBLE) {
      binding.buttonConfirm.visibility = View.GONE
      buttonVisible = true
    }
    if (binding.bottomLay.visibility == View.VISIBLE) {
      binding.bottomLay.visibility = View.GONE
      bottomLayVisible = false
    }
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
            if (buttonVisible) {
              binding.buttonConfirm.visibility = View.VISIBLE
            }
            if (bottomLayVisible) {
              binding.bottomLay.visibility = View.VISIBLE
            }
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

          if (_transaction.isDMTIndent()) {
            binding.unallocated =
              getString(string.unallocated_bulk_order) + (_transaction.unAllocatedVolume)?.toInt() + " MT"
          }
          if(_transaction!!.requiredAtDraw()!=null){
          binding.textDateTime.setCompoundDrawablesWithIntrinsicBounds(
              null, null, ContextCompat.getDrawable(
              this@BidDetailsActivity, _transaction!!.requiredAtDraw()!!
          ), null
          )
          }
        }


        if (binding.transaction?.indentOrigin.equals("LH")) {

          if (binding.transaction?.additionalRemarks.isNotNullOrEmpty()) {
            binding.remarks.visibility = View.VISIBLE
            binding.layRem.visibility = View.VISIBLE
            binding.div2.visibility = View.VISIBLE
            binding.remarks.text = binding.transaction?.additionalRemarks
          } else {
            binding.remarks.visibility = View.GONE
            binding.layRem.visibility = View.GONE
            binding.div2.visibility = View.GONE
          }

          if (binding.transaction?.indentHaltCenters.isNullOrEmpty()) {
            binding.stopNo.text = "No Stops"
          } else {
            binding.stopNo.text =
              binding.transaction?.indentHaltCenters!!.size.toString() + " Stops"
            for (i in binding.transaction?.indentHaltCenters!!.indices) {
              viewModel.fetchIndentCenters(
                binding.transaction?.indentHaltCenters!![i].haltCenterCode, i + 1
              )
            }
          }
        } else {

          if (binding.transaction?.orderCreationRemarks.isNotNullOrEmpty()) {
            binding.remarks.visibility = View.VISIBLE
            binding.layRem.visibility = View.VISIBLE
            binding.div2.visibility = View.VISIBLE
            binding.remarks.text = binding.transaction?.orderCreationRemarks
          } else {
            binding.remarks.visibility = View.GONE
            binding.layRem.visibility = View.GONE
            binding.div2.visibility = View.GONE

          }

          var total = 0

          if (binding.transaction?.pickupLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Pickup Address", binding.transaction?.pickupLocationAddress))
          } else {
            var uData = StringBuilder()
            if (binding.transaction?.loadingLocationPincode.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.loadingLocationPincode + "-")
            }

            if (binding.transaction?.pickupLocationCity.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.pickupLocationCity?.capitalize())
            }

            if (uData.toString().isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Address", uData.toString()))
            }

          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup1)) {
            total = total + 1
            binding.textViaDestination.card1.visibility = View.VISIBLE
            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = binding.transaction?.pickup1?.capitalize()
            } else {
              citNam = binding.transaction?.pickup1City?.capitalize()
            }
            binding.textViaDestination.city1.text = citNam

            if (binding.transaction?.pickup1Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", binding.transaction?.pickup1Address))
            } else {
              var uData = StringBuilder()
              if (binding.transaction?.pickup1AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.pickup1AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Pickup Intermediary Stop", uData.toString()))
              }
            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.pickup2)) {
            total = total + 1
            binding.textViaDestination.card2.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = binding.transaction?.pickup2?.capitalize()
            } else {
              citNam = binding.transaction?.pickup2City?.capitalize()
            }
            binding.textViaDestination.city2.text = citNam

            if (binding.transaction?.pickup2Address.isNotNullOrEmpty()) {
              uploadArray.add(Pair("Pickup Intermediary Stop", binding.transaction?.pickup2Address))
            } else {

              var uData = StringBuilder()
              if (binding.transaction?.pickup2AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.pickup2AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Pickup Intermediary Stop", uData.toString()))
              }

            }
          }

          if (!TextUtils.isEmpty(binding.transaction?.stop1)) {
            if (total == 0) {
              binding.textViaDestination.img3.visibility = View.GONE
            }
            total = total + 1
            binding.textViaDestination.card3.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = binding.transaction?.stop1?.capitalize()
            } else {
              citNam = binding.transaction?.stop1City?.capitalize()
            }
            binding.textViaDestination.city3.text = citNam

            if (binding.transaction?.intermediaryStop1Address.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Drop Intermediary Stop", binding.transaction?.intermediaryStop1Address
                )
              )
            } else {
              var uData = StringBuilder()
              if (binding.transaction?.intermediaryStop1AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.intermediaryStop1AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Drop Intermediary Stop", uData.toString()))
              }

            }

          }
          if (!TextUtils.isEmpty(binding.transaction?.stop2)) {
            total = total + 1
            binding.textViaDestination.card4.visibility = View.VISIBLE

            var citNam: String? = null
            if (binding.transaction?.isDmt == true) {
              citNam = binding.transaction?.stop2?.capitalize()
            } else {
              citNam = binding.transaction?.stop2City?.capitalize()
            }
            binding.textViaDestination.city4.text = citNam

            if (binding.transaction?.intermediaryStop2Address.isNotNullOrEmpty()) {
              uploadArray.add(
                Pair(
                  "Drop Intermediary Stop", binding.transaction?.intermediaryStop2Address
                )
              )
            } else {

              var uData = StringBuilder()
              if (binding.transaction?.intermediaryStop2AddressPin.isNotNullOrEmpty()) {
                uData.append(binding.transaction?.intermediaryStop2AddressPin + "-")
              }
              if (citNam.isNotNullOrEmpty()) {
                uData.append(citNam)
              }

              if (uData.toString().isNotNullOrEmpty()) {
                uploadArray.add(Pair("Drop Intermediary Stop", uData.toString()))
              }
            }
          }

          if (total > 0) {
            binding.textViaLabel.visibility = View.VISIBLE
            binding.stopNo.text = "$total Stops"
          } else {
            binding.stopNo.text = "No Stops"
          }

          if (binding.transaction?.dropLocationAddress.isNotNullOrEmpty()) {
            uploadArray.add(Pair("Drop Address", binding.transaction?.dropLocationAddress))
          } else {

            var uData = StringBuilder()
            if (binding.transaction?.unloadingLocationPincode.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.unloadingLocationPincode + "-")
            }

            if (binding.transaction?.dropLocationCity.isNotNullOrEmpty()) {
              uData.append(binding.transaction?.dropLocationCity?.capitalize())
            }

            if (uData.toString().isNotNullOrEmpty()) {
              uploadArray.add(Pair("Drop Address", uData.toString()))
            }

          }

          if (!uploadArray.isEmpty()) {
            binding.addressLay.visibility = View.VISIBLE
            val addressDetailAdapter = AddressDetailAdapter(uploadArray)
            binding.addresslist.apply {
              layoutManager = LinearLayoutManager(applicationContext)
              adapter = addressDetailAdapter
            }
          } else {
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
                if (binding.textBulkLoad.visibility == View.GONE) {
                  binding.timerLayout.visibility = View.VISIBLE
                  setTimer(bidEndingTime)
                }
                isFirstBid = true
                if (viewModel.refreshCalled == false) {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                }
                binding.status.visibility = View.GONE

                binding.buttonConfirm.text = "Place Bid"
                binding.bottomLay.visibility = View.VISIBLE
                binding.buttonConfirm.setOnClickListener {
                  bidDialog()
                }
              }
          }
          is BidDetailsUserBidState_PlaceBid -> {
            ViewBidDetailsPlaceBidBinding.inflate(layoutInflater, binding.containerActions, false)
              .apply {
                if (binding.textBulkLoad.visibility == View.GONE) {
                  binding.timerLayout.visibility = View.VISIBLE
                  setTimer(bidEndingTime)
                }
                isFirstBid = true
                bidsRecieved = state.bidsCount
                state.lowestAndUserBidPair.second?.let {
                  lowestBid = when (state.lowestAndUserBidPair) {
                    null -> ""
                    else -> "Lowest Bid: ₹ ${
                      StringUtils.formatAmount(
                        state.lowestAndUserBidPair.second?.bidAmount ?: 0.0
                      )
                    }" + if (state.isPMTIndent) "/MT" else ""
                  }
                }

                if (state.bidsCount != null && state.bidsCount > 0) {
                  binding.numBids.text = "$bidsRecieved Bids Received"
                  binding.numLay.visibility = View.VISIBLE
                } else {
                  binding.numLay.visibility = View.GONE
                }

                if (lowestBid.isNotNullOrEmpty()) {
                  binding.lowBid.text = lowestBid.toString()
                } else {
                  binding.lowBid.visibility = View.GONE
                }

                binding.buttonConfirm.text = "Place Bid"
                binding.bidLay.visibility = View.GONE
                binding.bottomLay.visibility = View.VISIBLE
                binding.buttonConfirm.setOnClickListener {
                  bidDialog()
                }
                if (viewModel.refreshCalled == false) {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(
                      PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
                      PROPERTY_SUB_SOURCE
                    ),
                    mutableListOf(
                      viewModel.transactionId,
                      state.bidsCount.toString(),
                      source,
                      subSource
                    )
                  )
                }
                binding.status.visibility = View.GONE
              }
          }
          is BidDetailsUserBidState_EditBid -> {
            ViewBidDetailsEditBidBinding.inflate(layoutInflater, binding.containerActions, false)
              .apply {
                binding.timerLayout.visibility = View.GONE
                val data = viewModel.transaction as HomeBidsRequestItemData
                data.numBids = state.bidsCount
                var oldAmount = data?.transactionBid?.bidAmount
                data.transactionBid = state.lowestAndUserBidPair.first
                bidsRecieved = state.bidsCount
                val userBid = state.lowestAndUserBidPair.first
                val lowestTBid = state.lowestAndUserBidPair.second
                lowestTBid?.let {
                  if (it.biddingType.compareTo(userBid?.biddingType ?: "") == 0) {
                    lowestBid = when (it) {
                      null -> ""
                      else -> "Lowest Bid: ₹ ${
                        StringUtils.formatAmount(
                          it.bidAmount
                        )
                      }" + if (state.isPMTIndent) "/MT" else ""
                    }
                    data.lowestBid = when (it) {
                      null -> 0.0
                      else -> it.bidAmount
                    }
                  }
                }
                if (viewModel.openConfirmBid) {
                  viewModel.openConfirmBid=false
                  BidConfirmReviseDialog(
                    this@BidDetailsActivity,data, viewModel, 0
                  ).show()
                }
                binding.numLay.visibility = View.GONE
                binding.bottomLay.visibility = View.VISIBLE

                if (userBid?.bidAmount != null) {
                  binding.bidLay.visibility = View.VISIBLE
                  if (state.isPMTIndent) {
                    binding.bidVal.text =
                      "₹ ${StringUtils.formatAmount(userBid?.bidAmount)}" + "/MT"
                  } else {
                    binding.bidVal.text = "₹ ${StringUtils.formatAmount(userBid?.bidAmount)}"
                  }
                  binding.greenBidLay.visibility = View.VISIBLE
                } else {
                  binding.bidLay.visibility = View.GONE
                }

                if (state.bidsCount != null && state.bidsCount > 0) {
                  binding.numBidsVal.text = "$bidsRecieved Bids Received"
                  binding.numBidsVal.visibility = View.VISIBLE
                  binding.greenBidLay.visibility = View.VISIBLE
                } else {
                  binding.numBidsVal.visibility = View.GONE
                }

                if (state.bidsCount != null && state.bidsCount > 1) {
                  if (lowestTBid?.bidAmount != null) {
                    if (userBid?.bidAmount?.equals(lowestTBid?.bidAmount) == true) {
                      binding.tvCorr.setBackground(
                        resources.getDrawable(
                          R.drawable.bg_all_round_corner_light_green_12
                        )
                      )
                      binding.imgCorr.visibility = View.VISIBLE
                      binding.tvCorr.text = "Your bid is the lowest"
                      binding.tvCorr.setTextColor(
                        resources.getColor(R.color.bid_placed_green)
                      )
                      binding.llBidStatus.background =  ContextCompat.getDrawable(this@BidDetailsActivity,
                        R.drawable.bg_all_round_corner_light_green_12
                      )
                    } else {
                      binding.tvCorr.setBackground(
                        resources.getDrawable(
                          R.drawable.bg_all_round_corner_light_pink_12
                        )
                      )
                      binding.llBidStatus.background = null
                      binding.imgCorr.visibility = View.GONE
                      binding.tvCorr.text = binding.transaction?.lowestbidText()
                      binding.tvCorr.setTextColor(resources.getColor(R.color.bid_placed_red))
                    }
                  } else {
                    binding.tvCorr.setBackground(
                      resources.getDrawable(
                        R.drawable.bg_all_round_corner_light_pink_12
                      )
                    )
                    binding.llBidStatus.background = null
                    binding.imgCorr.visibility = View.GONE
                    binding.tvCorr.text = binding.transaction?.benchmarkPriceText()
                    binding.tvCorr.setTextColor(resources.getColor(R.color.bid_placed_red))
                  }
                } else if (userBid?.bidAmount != null && state.bidsCount != null && state.bidsCount == 1 && binding.transaction?.guidancePrice != null) {
                  if (userBid.bidAmount.compareTo(binding.transaction?.guidancePrice!!) > 0) {
                    binding.tvCorr.setBackground(
                      resources.getDrawable(
                        R.drawable.bg_all_round_corner_light_pink_12
                      )
                    )
                    binding.llBidStatus.background = null
                    binding.imgCorr.visibility = View.GONE
                    binding.tvCorr.text = binding.transaction?.benchmarkPriceText()
                    binding.tvCorr.setTextColor(resources.getColor(R.color.bid_placed_red))
                  } else {
                    binding.tvCorr.setBackground(
                      resources.getDrawable(
                        R.drawable.bg_all_round_corner_light_green_12
                      )
                    )
                    binding.llBidStatus.background =  ContextCompat.getDrawable(this@BidDetailsActivity,
                      R.drawable.bg_all_round_corner_light_green_12
                    )
                    binding.imgCorr.visibility = View.VISIBLE
                    binding.tvCorr.text = "Your bid is the lowest"
                    binding.tvCorr.setTextColor(
                      resources.getColor(R.color.bid_placed_green)
                    )
                  }
                }

                binding.buttonConfirm.text = "Edit Bid"
                binding.buttonConfirm.setOnClickListener {
                  bidDialog(userBid)
                }

                request = data
                if ((!viewModel.restrictEventTrigger && !viewModel.refreshCalled) || isFirstBid) {
                  isFirstBid = false
                  if (!reviseInitiated) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_ORDER_DETAILS_BID_SUBMIT,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                        PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                        PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString(),
                        state.lowestAndUserBidPair.second?.bidAmount.toString(),
                        state.lowestAndUserBidPair.second?.expectedArrivalTimePickupRemark.toString(),
                        source,
                        subSource
                      )
                    )
                  } else {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_BID_REVISE_SUBMITTED,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT,
                        PROPERTY_ORDER_LOWEST_BID_VALUE,
                        PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW,
                        PROPERTY_SOURCE, PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString() ?: "", data?.lowestBid.toString() ?: " ",
                        oldAmount.toString() ?: "", data?.bidAmountValue()
                          .toString() ?: "", source, subSource
                      )
                    )
                    reviseInitiated = false
                  }
                } else {
                  if (viewModel.refreshCalled == false) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        viewModel.transactionId,
                        bidsRecieved.toString(),
                        source,
                        subSource
                      )
                    )
                  }
                  viewModel.refreshCalled = false
                }
                viewModel.restrictEventTrigger = false

                if (viewModel.analyticsBucket) {
                  if (data.oneVisibility() == View.VISIBLE || data.twoVisibility() == View.VISIBLE) {
                    analyticsUtil.trackEvent(
                      EVENT_BID_INLINE_PROMPT,
                      mutableListOf(
                        PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID,
                        PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE
                      ),
                      mutableListOf(
                        userPrefs.userId(), data.key(), userPrefs.demandType,
                        userPrefs.userPerformance
                      )
                    )
                  } else if (data.threeVisibility() == View.VISIBLE || data.fourVisibility() == View.VISIBLE) {
                    analyticsUtil.trackEvent(
                      EVENT_BID_REVISE_PROMPT,
                      mutableListOf(
                        PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID,
                        PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE
                      ),
                      mutableListOf(
                        userPrefs.userId(), data.key(), userPrefs.demandType,
                        userPrefs.userPerformance
                      )
                    )
                  }
                  viewModel.analyticsBucket = false
                }

                if (data.threeVisibility() == View.VISIBLE || data.fourVisibility() == View.VISIBLE) {
                  val mHandler = Handler()
                  var mRunnable: Runnable = Runnable { }
                  mRunnable = object : Runnable {
                    override fun run() {
                      btnEditBidInsider.setVisibility(View.VISIBLE)
                      //loading our custom made animations
                      val animation = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.fade_in
                      )
                      //starting the animation
                      btnEditBidInsider.startAnimation(animation)
                      val animation2 = AnimationUtils.loadAnimation(
                        applicationContext, R.anim.fade_out
                      )
                      btnEditBidInsider.startAnimation(animation2)
                      mHandler.postDelayed({
                        btnEditBidInsider.setVisibility(View.GONE)
                      }, 3000)
                      mHandler.postDelayed(mRunnable, 2000)

                    }
                  }
                  mHandler.post(mRunnable)
                }

                btnEditBidInsider.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_NOT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: ""
                    )
                  )
                  bidDialog(userBid)
                })
                textEditBid.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: "",
                      state.bidsCount.toString()
                    )
                  )
                  bidDialog(userBid)
                })
                textEditBid2.setOnClickListener(View.OnClickListener {
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_LOWEST_BID_CTA,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT),
                    mutableListOf(
                      state.lowestAndUserBidPair.second?.transactionId ?: "",
                      state.bidsCount.toString()
                    )
                  )
                  bidDialog(userBid)
                })
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_active)
                binding.status.setBackgroundColor(resources.getColor(R.color.status_active))
                binding.status.setTextColor(resources.getColor(R.color.status_active))
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
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )

                binding.status.visibility = View.VISIBLE
                val data = viewModel.transaction as HomeBidsRequestItemData

                if (data.clientConfirmationPending == false) {
                  binding.status.text = resources.getString(R.string.label_pending)
                  binding.status.setTextColor(resources.getColor(R.color.pending))
                  binding.bottomLay.visibility = View.GONE
                } else {
                  binding.status.text = resources.getString(R.string.label_confirm)
                  binding.buttonConfirm.text = "View Trip"
                  binding.status.setBackground(
                    resources.getDrawable(
                      R.drawable.bg_all_round_corner_light_green_12
                    )
                  )
                  binding.status.setTextColor(resources.getColor(R.color.bid_placed_green))
                  binding.bidLay.visibility = View.GONE
                  binding.numLay.visibility = View.GONE
                  binding.bottomLay.visibility = View.VISIBLE
                  binding.buttonConfirm.setOnClickListener {
                    startActivity(
                      tripDetailsIntent(
                        viewModel.transaction.uuid.toString(), this@BidDetailsActivity
                      )
                    )
                  }

                }
              }
          }
          is BidDetailsUserBidState_RejectedBid -> {
            ViewBidDetailsRejectedBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                binding.bottomLay.visibility = View.GONE
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                textUserHighestBid.text = bidText
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_lost)
                binding.textTargetPrice.text = "₹" + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0)
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                binding.textTargetPriceLabel.text = "Your Bid"
                binding.priceLay.visibility = View.VISIBLE
                binding.status.setBackgroundColor(resources.getColor(R.color.status_lost_bg))
                binding.status.setTextColor(resources.getColor(R.color.status_lost_bid))
                binding.bottomLay.visibility = View.GONE
              }
          }
          is BidDetailsUserBidState_CancelledBid -> {
            ViewBidDetailsCancelledBidBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE
                binding.bottomLay.visibility = View.GONE
                binding.bidLay.visibility = View.GONE
                val bidText = getString(string.msg_your_bid) + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.bidAmount ?: 0.0) + "/MT"
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                if (viewModel.refreshCalled == false)
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                    mutableListOf(viewModel.transactionId, source, subSource)
                  )
                textUserHighestBid.text = bidText
                binding.status.visibility = View.VISIBLE
                binding.status.text = resources.getString(R.string.label_cancel)
                binding.textTargetPrice.text = "₹" + if (state.isPMTIndent) {
                  StringUtils.formatAmount(state.userBid.pmtRate ?: 0.0)
                } else {
                  StringUtils.formatAmount(state.userBid.bidAmount)
                }
                binding.textTargetPriceLabel.text = "Your Bid"
                binding.priceLay.visibility = View.VISIBLE
                binding.status.setTextColor(resources.getColor(R.color.status_lost))
                binding.bottomLay.visibility = View.GONE
              }
          }

          is BidDetailsUserBidState_BulkLoad_Edit -> {
            ViewBidDetailsBulkLoadEditBinding.inflate(
              layoutInflater, binding.containerActions, false
            )
              .apply {
                binding.timerLayout.visibility = View.GONE

                val data = viewModel.transaction as HomeBidsRequestItemData
                var bidAmount = ""
                var expectedArrivalPickup = ""
                if (data.transactionStatus == "cancelled") {
                  btnReviseBidInsider.visibility = View.GONE
                  reduceText.visibility = View.GONE
                }
                data.bulkTransactionBids = state.bids
                bidsRecieved = state.bidsCount
                data.let {
                  if (!it.bulkTransactionBids.isNullOrEmpty()) {
                    if (viewModel.restrictEventTrigger && viewModel.refreshCalled == false) {
                      analyticsUtil.moEngageTrackEvent(
                        EVENT_PAGE_LOAD_ORDER_DETAILS_WITH_EXISTING_BID,
                        mutableListOf(
                          PROPERTY_ORDER_ID,
                          PROPERTY_BID_COUNT,
                          PROPERTY_SOURCE,
                          PROPERTY_SUB_SOURCE
                        ),
                        mutableListOf(
                          viewModel.transactionId,
                          bidsRecieved.toString(),
                          source,
                          subSource
                        )
                      )
                    }
                  } else {
                    if (viewModel.restrictEventTrigger && viewModel.refreshCalled == false)
                      analyticsUtil.moEngageTrackEvent(
                        EVENT_PAGE_LOAD_ORDER_DETAILS_WITHOUT_EXISTING_BID,
                        mutableListOf(PROPERTY_ORDER_ID, PROPERTY_SOURCE, PROPERTY_SUB_SOURCE),
                        mutableListOf(viewModel.transactionId, source, subSource)
                      )
                  }
                }
                data.let {
                  if (!it.bulkTransactionBids.isNullOrEmpty()) {
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

                binding.bidLay.visibility = View.GONE
                binding.bottomLay.visibility = View.GONE

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
                if ((!viewModel.restrictEventTrigger && !viewModel.refreshCalled) || isFirstBid) {
                  isFirstBid = false
                  if (!reviseInitiated) {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_ORDER_DETAILS_BID_SUBMIT,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                        PROPERTY_VEHICLE_REPORTING_DATE_TIME,
                        PROPERTY_SOURCE, PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString(),
                        bidAmount,
                        expectedArrivalPickup,
                        source, subSource
                      )
                    )
                  } else {
                    analyticsUtil.moEngageTrackEvent(
                      EVENT_BID_REVISE_SUBMITTED,
                      mutableListOf(
                        PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
                        PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW,
                        PROPERTY_SOURCE,
                        PROPERTY_SUB_SOURCE
                      ),
                      mutableListOf(
                        state.lowestAndUserBidPair.second?.transactionId ?: "",
                        state.bidsCount.toString() ?: "",
                        state.lowestAndUserBidPair?.first?.bidAmount?.toString() ?: " ",
                        oldAmountbids,
                        bidAmount,
                        source,
                        subSource
                      )
                    )
                    reviseInitiated = false
                  }
                } else {
                  viewModel.refreshCalled = false
                }
                viewModel.restrictEventTrigger = false
              }
          }
          else -> null
        }?.let { _binding ->
          /* bidding ended */
//          binding.textBidEnded.visible(_binding is ViewBidDetailsRejectedBidBinding)
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
          if (viewModel.dmtStatus == "dmt" || it.isDMTIndent()) {
            uiUtils.showProgress()
            viewModel.fetchTruckType(it);
          } else {
            if (it.transactionBid == null) {
              analyticsUtil.moEngageTrackEvent(
                EVENT_ORDER_DETAILS_BID_INITIATE,
                mutableListOf(
                  PROPERTY_ORDER_ID,
                  PROPERTY_BID_COUNT,
                  PROPERTY_SOURCE,
                  PROPERTY_SUB_SOURCE
                ),
                mutableListOf(it?.uuid ?: "", viewModel.bidCount.toString(), source, subSource)
              )
              reviseInitiated = false
            } else {
              analyticsUtil.moEngageTrackEvent(
                EVENT_BID_REVISE_INITIATED,
                mutableListOf(
                  PROPERTY_ORDER_ID,
                  PROPERTY_BID_COUNT,
                  PROPERTY_ORDER_LOWEST_BID_VALUE,
                  PROPERTY_SOURCE,
                  PROPERTY_SUB_SOURCE
                ),
                mutableListOf(
                  it?.uuid.toString(), viewModel.bidCount.toString(),
                  viewModel.lowestBid.toString(), source, subSource
                )
              )
              reviseInitiated = true
            }
            BidDetailsCreateEditDialog(
              this, it, bid, viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs,
              fromPage = "load_detail"
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

  override fun handleAction(
    actionId: String,
    position: Int,
    item: BaseBulkBidSummaryRVAdapterItem<*>
  ) {
    when(actionId){
      EXPAND_CARD -> {
        val bidData = item.data as BulkBidSummaryItemData
        bidData.expanded = !bidData.expanded
        BidDetailsViewModel.truckNumTextViewAdded = !BidDetailsViewModel.truckNumTextViewAdded
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
        source:String?= VALUE_APP_FLOW,
        subSource:String?="NA"
) = Intent(context, BidDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
  if(requestType!=null)
    putExtra(RequestTypeIntentKey, requestType)
  putExtra(FromPage,fromBidsPage)
  putExtra(ActiveBid,active)
  putExtra(PROPERTY_SOURCE,source)
  putExtra(PROPERTY_SUB_SOURCE,subSource)
}
package com.delhivery.axle.ui.tripdetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.ChargeType
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.data.BalancePaid
import com.delhivery.axle.data.PODUploaded
import com.delhivery.axle.data.TripHistoryItem
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.ActivityTripDetailsBinding
import com.delhivery.axle.databinding.ViewPaymentSummaryItemBinding
import com.delhivery.axle.databinding.ViewTripHistoryItemBinding
import com.delhivery.axle.databinding.ViewTripHistoryPodUploadedBinding
import com.delhivery.axle.databinding.ViewTripPaymentSummaryBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.EVENT_PAYMENT_SUMMARY
import com.delhivery.axle.utils.EVENT_TRIP_STATUS_HISTORY
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.VALUE_LOAD

class TripDetailsActivity : BaseActivity<ActivityTripDetailsBinding, TripDetailsViewModel>() {

  override fun getViewModelClass() = TripDetailsViewModel::class.java

  override fun layoutId() = R.layout.activity_trip_details

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
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe trip details live data */
    viewModel.progressLiveData.observe(this, ProgressObserver())

    viewModel.tripLiveData.observe(this, Observer {
      it?.apply {
        title = first.tripDisplayName(second.tripStatus())
        binding.transactionDetails = first
        binding.tripDetails = second
        viewModel.bidDetail = second.bidDetails
        viewModel.fetchWarehouseDetails()
      }
    })

    viewModel.warehouseLiveData.observe(this, Observer {
      binding.labelWarehouse.text = it
    })

    viewModel.paymentLiveData.observe(this, Observer {
      if (it) {
        binding.progressHistory.root.visibility = View.GONE
        populateHistory(viewModel.tripHistory.toMutableList())
      }
    })

    binding.viewHistory.setOnClickListener {
      populateHistory(viewModel.tripHistory.toMutableList())
    }

    binding.viewSummary.setOnClickListener {
      populatePaymentSummary(
          viewModel.paymentSummary.toMutableList()
      )
    }

    /* fetch trip details and payment summary */
    viewModel.fetchTripDetails()
    viewModel.fetchPaymentSummary()
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
   * Populate trip history
   */
  private fun populateHistory(history: MutableList<TripHistoryItem>) {
    // Capture event
    analyticsUtil.trackEvent(
        EVENT_TRIP_STATUS_HISTORY,
        mutableListOf(PROPERTY_TRANSACTION_ID),
        mutableListOf(viewModel.transactionId)
    )
    binding.viewHistory.isSelected = true
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewSummary.isSelected = false
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    var index = 0
    history.forEach { item ->
      when (item.id) {
        BalancePaid -> {
          ViewTripHistoryItemBinding.inflate(
              layoutInflater, binding.containerHistory, false
          )
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                binding.containerHistory.addView(root)
              }
        }
        PODUploaded -> {
          ViewTripHistoryPodUploadedBinding.inflate(layoutInflater, binding.containerHistory, false)
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                textInvoice.setOnClickListener {
                  it.post {
                    startActivity(imageViewIntent(it.context, item.podUrl, "View POD"))
                  }
                }
                binding.containerHistory.addView(root)
              }
        }
        else -> {
          ViewTripHistoryItemBinding.inflate(layoutInflater, binding.containerHistory, false)
              .apply {
                focusView = false
                if (index == 0) {
                  val background = item.getBackground()
                  container.setBackgroundResource(background)
                  focusView = background != R.color.white
                }
                setHistory(item)
                binding.containerHistory.addView(root)
              }
        }
      }
      index++
    }
  }

  /**
   * Populate payment summary
   */
  private fun populatePaymentSummary(tripChargesSummary: MutableList<TripChargesResponse>) {
    // Capture event
    analyticsUtil.trackEvent(
        EVENT_PAYMENT_SUMMARY,
        mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
        mutableListOf(VALUE_LOAD, viewModel.transactionId)
    )
    binding.viewSummary.isSelected = true
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewHistory.isSelected = false
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    val paymentSummaryBinding = ViewTripPaymentSummaryBinding.inflate(
        layoutInflater, binding.containerHistory, false
    )

    var total = 0.0
    tripChargesSummary.add(
        0, TripChargesResponse(
        "", ChargeType.Freight.charge_key,
        0.0, binding.tripDetails?.bidDetails?.bidPrice?.toDouble() ?: 0.0,
        "", ""
    )
    )

    tripChargesSummary.forEach { _payment ->
      if (_payment.payVendor > 0) {
        ViewPaymentSummaryItemBinding.inflate(
            layoutInflater, paymentSummaryBinding.containerPayment, false
        )
            .apply {
              data = _payment
              seprator.visibility = View.GONE
              if (_payment.head.compareTo(ChargeType.Damages.charge_key) == 0) {
                textChargeValue.setTextColor(
                    ContextCompat.getColor(this@TripDetailsActivity, R.color.status_lost)
                )
                total -= _payment.payVendor
              } else {
                total += _payment.payVendor
              }

              paymentSummaryBinding.containerPayment.addView(root)
            }
      }
    }

    ViewPaymentSummaryItemBinding.inflate(
        layoutInflater, paymentSummaryBinding.containerPayment, false
    )
        .apply {
          seprator.visibility = View.VISIBLE
          data = TripChargesResponse(
              "", ChargeType.SubTotal.charge_key,
              0.0, total,
              "", ""
          )
          paymentSummaryBinding.containerPayment.addView(root)
        }

    ViewPaymentSummaryItemBinding.inflate(
        layoutInflater, paymentSummaryBinding.containerPayment, false
    )
        .apply {
          seprator.visibility = View.GONE
          data = TripChargesResponse(
              "", ChargeType.TDS.charge_key,
              0.0, total * (100 - viewModel.userPrefs.tdsRate) / 100,
              "", ""
          )
          textChargeValue.setTextColor(
              ContextCompat.getColor(this@TripDetailsActivity, R.color.status_lost)
          )

          paymentSummaryBinding.containerPayment.addView(root)
        }

    paymentSummaryBinding.total =
      "₹ ${StringUtils.formatAmount(total * viewModel.userPrefs.tdsRate / 100)}"

    val advance =
      when (binding.tripDetails?.advanceDeduction()) {
        true -> {
          (viewModel.bidDetail?.advancePayout?.times(viewModel.userPrefs.tdsRate))?.div(100) ?: 0.0
        }
        else -> {
          viewModel.bidDetail?.advancePayout ?: 0.0
        }
      }

    if (advance > 0.0) {
      paymentSummaryBinding.containerAdvance.visibility = View.VISIBLE
      paymentSummaryBinding.advance = when (viewModel.advancePaid) {
        true -> {
          paymentSummaryBinding.labelAdvance.text = getString(string.label_advance_paid)
          "₹ ${StringUtils.formatAmount(advance)}"
        }
        false -> {
          paymentSummaryBinding.labelAdvance.text = getString(string.label_advance_pending)
          "₹ ${StringUtils.formatAmount(advance)}"
        }
      }
    } else {
      paymentSummaryBinding.containerAdvance.visibility = View.GONE
    }

    val balance = (total * viewModel.userPrefs.tdsRate / 100).minus(advance)
    paymentSummaryBinding.balance = when (viewModel.balancePaid) {
      true -> {
        paymentSummaryBinding.labelBalance.text = getString(string.label_balance_paid)
        "₹ ${StringUtils.formatAmount(balance)}"
      }
      false -> {
        paymentSummaryBinding.labelBalance.text = getString(string.label_balance_pending)
        "₹ ${StringUtils.formatAmount(balance)}"
      }
    }
    paymentSummaryBinding.containerTotal.visibility = View.VISIBLE

    binding.containerHistory.addView(paymentSummaryBinding.root)
  }
}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Trip details intent
 */
fun tripDetailsIntent(
  _data: HomeTripsItemData,
  context: Context
) = Intent(context, TripDetailsActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, _data.key())
}


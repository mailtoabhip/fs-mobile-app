package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.api.response.ChargeType
import com.delhivery.orion.api.response.TripChargesResponse
import com.delhivery.orion.config.UrlConfig
import com.delhivery.orion.data.BalancePaid
import com.delhivery.orion.data.PODUploaded
import com.delhivery.orion.data.TripHistoryItem
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.databinding.ActivityTripDetailsBinding
import com.delhivery.orion.databinding.ViewPaymentSummaryItemBinding
import com.delhivery.orion.databinding.ViewTripHistoryItemBinding
import com.delhivery.orion.databinding.ViewTripHistoryPodUploadedBinding
import com.delhivery.orion.databinding.ViewTripPaymentSummaryBinding
import com.delhivery.orion.ui.base.BaseActivity

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
        title = first.tripDisplayName()
        binding.transactionDetails = first
        binding.tripDetails = second
        viewModel.bidDetail = second.bidDetails
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
    binding.viewHistory.isSelected = true
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewSummary.isSelected = false
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    var index = 0
    history.forEach { item ->
      when (item.id) {
        BalancePaid -> {
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
                    startActivity(
                        imageViewIntent(
                            it.context, "${UrlConfig.TripService.url()}/${item.podUrl}", "View POD"
                        )
                    )
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

    paymentSummaryBinding.total = "₹ ${String.format("%.2f", total)}"

    val advance = viewModel.bidDetail?.advancePayout ?: 0.0
    if (advance > 0.0) {
      paymentSummaryBinding.containerAdvance.visibility = View.VISIBLE
      paymentSummaryBinding.advance = when (viewModel.advancePaid) {
        true -> {
          paymentSummaryBinding.labelAdvance.text = "Advance Paid"
          "₹ ${String.format("%.2f", advance)}"
        }
        false -> {
          paymentSummaryBinding.labelAdvance.text = "Advance Pending"
          "₹ ${String.format("%.2f", advance)}"
        }
      }
    } else {
      paymentSummaryBinding.containerAdvance.visibility = View.GONE
    }

    val balance = total.minus(advance)
    paymentSummaryBinding.balance = when (viewModel.balancePaid) {
      true -> {
        paymentSummaryBinding.labelBalance.text = "Balance Paid"
        "₹ ${String.format("%.2f", balance)}"
      }
      false -> {
        paymentSummaryBinding.labelBalance.text = "Balance Pending"
        "₹ ${String.format("%.2f", balance)}"
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


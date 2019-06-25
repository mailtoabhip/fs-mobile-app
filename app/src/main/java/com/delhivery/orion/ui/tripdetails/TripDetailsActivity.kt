package com.delhivery.orion.ui.tripdetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.api.response.ChargeType
import com.delhivery.orion.api.response.PaymentResponse
import com.delhivery.orion.data.TripHistoryModel
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.databinding.ActivityTripDetailsBinding
import com.delhivery.orion.databinding.ViewPaymentSummaryItemBinding
import com.delhivery.orion.databinding.ViewTripHistoryItemBinding
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
        populateHistory(third)
      }
    })

    binding.viewHistory.setOnClickListener {
      populateHistory(viewModel.tripHistory)
    }

    binding.viewSummary.setOnClickListener {
      populatePaymentSummary(
          viewModel.paymentSummary
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
  private fun populateHistory(history: List<TripHistoryModel>) {
    binding.viewHistory.isSelected = true
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewSummary.isSelected = false
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    val iterator = history.listIterator()
    while (iterator.hasNext()) {
      ViewTripHistoryItemBinding.inflate(layoutInflater, binding.containerHistory, false)
          .apply {
            if (!iterator.hasPrevious()) {
              container.setBackgroundResource(R.drawable.bg_trip_meter_gradient)
            }
            setHistory(iterator.next())
            binding.containerHistory.addView(root)
          }
    }
  }

  /**
   * Populate payment summary
   */
  private fun populatePaymentSummary(paymentSummary: List<PaymentResponse>) {
    binding.viewSummary.isSelected = true
    binding.textPaymentSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
    binding.viewHistory.isSelected = false
    binding.textStatusHistory.setTextColor(ContextCompat.getColor(this, R.color.transparent_grey))

    binding.containerHistory.removeAllViews()
    val paymentSummaryBinding = ViewTripPaymentSummaryBinding.inflate(
        layoutInflater, binding.containerHistory, false
    )

    var total = 0.0
    paymentSummary.forEach { _payment ->
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

    paymentSummaryBinding.textTotal.setText("₹ ${String.format("%.2f", total)}")
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


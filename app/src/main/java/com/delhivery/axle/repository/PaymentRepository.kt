package com.delhivery.axle.repository

import com.delhivery.axle.api.PaymentService
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
  private val paymentService: PaymentService
) : BaseRepository() {

  /**
   * User/supplier trip summary [BidSummaryResponse]
   */
  fun chargesSummary(
    transactionId: String
  ) = paymentService
      .chargesSummary(transactionId)
      .convertResponse()

  /**
   * Get bulk transactions using ids
   */
  fun bulkPaymentTransactions(
    trips: List<HomeTripsItemData>
  ) =
    paymentService.bulkTransactions(
        trips.map { it.transactionId }.joinToString(",") { it }
    )
        .convertResponse()
        .map { Pair(trips, it.payments) }

}
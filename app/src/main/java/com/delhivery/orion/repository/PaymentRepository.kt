package com.delhivery.orion.repository

import com.delhivery.orion.api.PaymentService
import com.delhivery.orion.utils.extensions.convertResponse
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
}
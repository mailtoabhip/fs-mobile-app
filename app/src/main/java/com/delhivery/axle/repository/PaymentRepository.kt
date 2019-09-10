package com.delhivery.axle.repository

import com.delhivery.axle.api.PaymentService
import com.delhivery.axle.api.TripService
import com.delhivery.axle.api.response.BulkPaymentItem
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.api.response.TripPaymentsResponse
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.extensions.convertResponse
import io.reactivex.Single
import io.reactivex.functions.Function3
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
  private val paymentService: PaymentService,
  private val tripsService: TripService
) : BaseRepository() {

  /**
   * Fetch Trip's charges summary [TripChargesResponse]
   */
  fun chargesSummary(
    transactionId: String
  ): Single<Triple<List<TripHistoryModel>, List<TripChargesResponse>, List<TripPaymentsResponse>>> =
    Single.zip(
        tripsService.tripHistory(transactionId).convertResponse(),
        paymentService.chargesSummary(transactionId).convertResponse(),
        paymentService.tripPayments(transactionId).convertResponse(),
        Function3<List<TripHistoryModel>, List<TripChargesResponse>, List<TripPaymentsResponse>,
            Triple<List<TripHistoryModel>, List<TripChargesResponse>, List<TripPaymentsResponse>>> { t1, t2, t3 ->
          Triple(t1, t2, t3)
        }
    )

  /**
   * Get bulk transactions using ids
   */
  fun bulkPaymentTransactions(
    trips: List<HomeTripsItemData>
  ): Single<Pair<List<HomeTripsItemData>, List<BulkPaymentItem>>> =
    paymentService.bulkTransactions(
        trips.map { it.transactionId }.joinToString(",") { it }
    )
        .convertResponse()
        .map { Pair(trips, it.payments) }

}
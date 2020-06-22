package com.delhivery.axle.api.repository

import com.delhivery.axle.api.response.BulkPaymentItem
import com.delhivery.axle.api.response.TripPaymentsResponse
import com.delhivery.axle.api.service.PaymentService
import com.delhivery.axle.api.service.TripService
import com.delhivery.axle.data.TripHistoryModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.extensions.convertResponse
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
  private val paymentService: PaymentService,
  private val tripsService: TripService
) : BaseRepository() {

  /**
   * Fetch Trip's history and payments summary
   */
  fun historyAndPayments(
    transactionId: String
  ): Single<Pair<List<TripHistoryModel>, List<TripPaymentsResponse>>> =
    Single.zip(
        tripsService.tripHistory(transactionId)
            .convertResponse(),
        paymentService.tripPayments(transactionId)
            .convertResponse(),
        BiFunction<List<TripHistoryModel>, List<TripPaymentsResponse>,
            Pair<List<TripHistoryModel>, List<TripPaymentsResponse>>> { t1, t2 ->
          Pair(t1, t2)
        }
    )

  /**
   * Get bulk transactions using ids
   */
  fun bulkPaymentTransactions(
    trips: List<HomeTripsItemData>
  ): Single<Pair<List<HomeTripsItemData>, List<BulkPaymentItem>>> =
    paymentService.bulkTransactions(
        trips.map { it.transactionId }
            .joinToString(",") { it }
    )
        .convertResponse()
        .map { Pair(trips, it.payments) }

}
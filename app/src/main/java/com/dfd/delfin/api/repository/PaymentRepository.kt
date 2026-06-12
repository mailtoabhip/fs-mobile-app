package com.dfd.delfin.api.repository

import com.dfd.delfin.api.response.BulkPaymentItem
import com.dfd.delfin.api.response.TripPaymentsResponse
import com.dfd.delfin.api.service.PaymentService
import com.dfd.delfin.api.service.TripService
import com.dfd.delfin.data.TripHistoryModel
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
  private val paymentService: PaymentService,
  private val tripsService: TripService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

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
     * Fetch Payments Summary
     */
    fun payments(
            transactionId: String
    ) = paymentService.payments(transactionId).convertResponse()

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
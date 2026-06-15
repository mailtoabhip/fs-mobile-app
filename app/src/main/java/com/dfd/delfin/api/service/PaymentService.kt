package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.PaymentsResponse
import com.dfd.delfin.api.response.TripPaymentsBulkResponse
import com.dfd.delfin.api.response.TripPaymentsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to Payment Service
 */
interface PaymentService {

  /**
   * Payment details
   */
  @GET("payments/{transactionId}/")
  fun tripPayments(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<TripPaymentsResponse>>>

  /**
   * Bulk transaction ids
   *
   * @param transactionIds Comma separated ids
   */
  @GET("/payments/summary")
  fun bulkTransactions(
    @Query("transaction_ids") transactionIds: String
  ): Single<BaseResponse<TripPaymentsBulkResponse>>

  /**
   * New Payments
    */

  @GET("payments/{transactionId}/")
  fun payments(
          @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<PaymentsResponse>>>


}
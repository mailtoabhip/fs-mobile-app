package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.api.response.TripPaymentsBulkResponse
import com.delhivery.axle.api.response.TripPaymentsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentService {

  /**
   * Charges details
   * @return List<TripChargeResponse>
   */
  @GET("charges/{transactionId}/")
  fun chargesSummary(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<TripChargesResponse>>>

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

}
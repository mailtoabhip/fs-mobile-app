package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TripChargesResponse
import com.delhivery.axle.api.response.TripPaymentsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentService {

  /**
   * Payment details
   */
  @GET("charges/{transactionId}/")
  fun chargesSummary(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<TripChargesResponse>>>

  /**
   * Bulk transaction ids
   *
   * @param transactionIds Comma separated ids
   */
  @GET("/payments/summary")
  fun bulkTransactions(
    @Query("transaction_ids") transactionIds: String
  ): Single<BaseResponse<TripPaymentsResponse>>

}
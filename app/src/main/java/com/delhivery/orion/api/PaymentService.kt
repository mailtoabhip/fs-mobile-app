package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.PaymentResponse
import com.delhivery.orion.api.response.TransactionsResponse
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
  ): Single<BaseResponse<List<PaymentResponse>>>

  /**
   * Bulk transaction ids
   *
   * @param transactionIds Comma separated ids
   */
  @GET("/payments/summary")
  fun bulkTransactions(
    @Query("transactions_ids") transactionIds: String
  ): Single<BaseResponse<TransactionsResponse>>

}
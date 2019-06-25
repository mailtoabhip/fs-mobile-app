package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.PaymentResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface PaymentService {

  /**
   * Payment details
   */
  @GET("charges/{transactionId}/")
  fun chargesSummary(
    @Path("transactionId") transactionId: String
  ): Single<BaseResponse<List<PaymentResponse>>>

}
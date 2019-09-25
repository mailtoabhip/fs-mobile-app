package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FuelCardsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface FuelService {

  /**
   * Fetch active fuel cards
   */
  @GET("api/v1/iocl/get_cards/{walletId}")
  fun fetchActiveFuelCards(
    @Path("walletId") walletId: String
  ): Single<BaseResponse<FuelCardsResponse>>

}
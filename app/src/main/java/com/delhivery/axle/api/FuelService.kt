package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FuelCardsResponse
import io.reactivex.Single
import retrofit2.http.GET

interface FuelService {

  /**
   * Fetch active fuel cards
   */
  @GET("iocl/get_cards")
  fun fetchActiveFuelCards(): Single<BaseResponse<FuelCardsResponse>>

}
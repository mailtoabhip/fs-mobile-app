package com.delhivery.axle.api

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FuelCardsResponse
import com.delhivery.axle.data.fuelcards.FuelCardData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to Fuel Service
 */
interface FuelService {

  /**
   * Fetch active fuel cards
   */
  @GET("api/v1/iocl/get_cards/{walletId}")
  fun fetchActiveFuelCards(
    @Path("walletId") walletId: String
  ): Single<BaseResponse<FuelCardsResponse>>

  /**
   * Fetch fuel card by [tripId] [pan]
   */
  @GET("api/v1/iocl/get_card_balance/")
  fun fetchFuelCard(
    @Query("trip_id") tripId: String,
    @Query("pan") pan: String,
    @Query("wallet_id") walletId: String
  ): Single<BaseResponse<FuelCardData>>

}
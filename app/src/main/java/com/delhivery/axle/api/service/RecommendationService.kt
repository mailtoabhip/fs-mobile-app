package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.FuelPayoutResponse
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TripMeterResponse
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.*

/**
 * Handle network calls to Transaction Service
 */
interface RecommendationService {

  @GET("/")
  fun supplierRecommendation(
          @Query("sp_id") userId: String,
          @Query("offset") offset: Int,
          @Query("limit") limit: Int
           ): Single<BaseResponse<TransactionsResponse>>
}
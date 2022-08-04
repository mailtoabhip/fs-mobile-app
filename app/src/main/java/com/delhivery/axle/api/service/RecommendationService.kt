package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.FuelPayoutResponse
import com.delhivery.axle.api.request.ReccomdationRequest
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

    /**
     * Recommendation transactions
     */
    @POST("/get_sp_loads")
    fun recommendationTransactions(
      @Body request: ReccomdationRequest
    ): Single<BaseResponse<TransactionsResponse>>
}
package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.ReccomdationRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TransactionsResponse

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

    @POST("/get_sp_intracity_loads")
    fun recommendationIntracityTransactions(
        @Body request: ReccomdationRequest
    ): Single<BaseResponse<TransactionsResponse>>


}
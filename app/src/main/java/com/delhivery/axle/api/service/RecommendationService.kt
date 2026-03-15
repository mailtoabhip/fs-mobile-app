package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.ReccomdationRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TransactionsResponse

import retrofit2.http.*

/**
 * Handle network calls to Transaction Service
 */
interface RecommendationService {

    /**
     * Recommendation transactions - Coroutine version
     */
    @POST("/get_sp_loads")
    suspend fun recommendationTransactions(
      @Body request: ReccomdationRequest
    ): BaseResponse<TransactionsResponse>

    /**
     * Intracity recommendation transactions - Coroutine version
     */
    @POST("/get_sp_intracity_loads")
    suspend fun recommendationIntracityTransactions(
        @Body request: ReccomdationRequest
    ): BaseResponse<TransactionsResponse>

}
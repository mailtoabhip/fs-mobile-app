package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.ReccomdationRequest
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.TransactionsResponse

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
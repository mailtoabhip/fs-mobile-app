package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.InitiateCallRequest
import com.delhivery.axle.api.response.InitiateCallResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handle network calls to Spot Bidding Service for Marketplace
 */
interface SpotBiddingService {
    
    /**
     * Initiate call for marketplace bidding
     * 
     * @param request Request containing source, transaction_id, and bid_id
     * @return Response with bridge number details
     */
    @POST("marketplace/initiate-call")
    fun initiateMarketplaceCall(
        @Body request: InitiateCallRequest
    ): Single<InitiateCallResponse>
}


package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.InitiateCallRequest
import com.delhivery.axle.api.response.InitiateCallResponse
import com.delhivery.axle.api.service.SpotBiddingService
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Spot Bidding Marketplace operations
 */
@Singleton
class SpotBiddingRepository @Inject constructor(
    private val spotBiddingService: SpotBiddingService
) : BaseRepository() {

    /**
     * Initiate a call for marketplace bidding
     *
     * @param transactionId The transaction ID
     * @param bidId The bid ID
     * @param source The source (default: "axle_marketplace")
     * @return Single with bridge number response
     */
    fun initiateMarketplaceCall(
        transactionId: String,
        bidId: String,
        source: String = "axle_marketplace"
    ): Single<InitiateCallResponse> {
        val request = InitiateCallRequest(
            source = source,
            transactionId = transactionId,
            bidId = bidId
        )
        return spotBiddingService.initiateMarketplaceCall(request)
    }
}


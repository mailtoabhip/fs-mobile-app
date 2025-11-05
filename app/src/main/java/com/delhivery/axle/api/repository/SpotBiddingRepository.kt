package com.delhivery.axle.api.repository

import android.util.Log
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
     * @param source The source mode: "marketplace" (default) or "default"
     * @param deviceSimNumbers Optional device SIM numbers (max 2)
     * @return Single with bridge number response
     */
    fun initiateMarketplaceCall(
        transactionId: String,
        bidId: String,
        source: String = "marketplace",
        deviceSimNumbers: List<String>? = null
    ): Single<InitiateCallResponse> {
        val request = InitiateCallRequest(
            source = source,
            transactionId = transactionId,
            bidId = bidId,
            deviceSimNumbers = deviceSimNumbers
        )
        
        return spotBiddingService.initiateMarketplaceCall(request)
            .doOnError { error ->
                Log.e("SpotBiddingRepository", "Marketplace call initiation failed: ${error.message}", error)
            }
    }
}


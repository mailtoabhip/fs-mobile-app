package com.dfd.delfin.api.repository

import android.util.Log
import com.dfd.delfin.api.request.InitiateCallRequest
import com.dfd.delfin.api.response.InitiateCallResponse
import com.dfd.delfin.api.service.SpotBiddingService
import com.dfd.delfin.utils.ErrorLogger
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Spot Bidding Marketplace operations
 */
@Singleton
class SpotBiddingRepository @Inject constructor(
    private val spotBiddingService: SpotBiddingService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

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
        source: String = "axle_marketplace",
        deviceSimNumbers: List<String>? = null
    ): Single<InitiateCallResponse> {
        val request = InitiateCallRequest(
            source = "axle_marketplace",
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


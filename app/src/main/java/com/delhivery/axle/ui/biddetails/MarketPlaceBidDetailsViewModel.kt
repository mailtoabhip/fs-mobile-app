package com.delhivery.axle.ui.biddetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.SpotBiddingRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.response.InitiateCallResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * ViewModel for MarketPlace Bid Details screen
 */
class MarketPlaceBidDetailsViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val bidsRepository: BidsRepository,
    private val spotBiddingRepository: SpotBiddingRepository
) : BaseViewModel() {

    private val _transactionLiveData = MutableLiveData<HomeBidsRequestItemData?>()
    val transactionLiveData: LiveData<HomeBidsRequestItemData?> = _transactionLiveData

    private val _bidPlacementResultLiveData = MutableLiveData<BidPlacementResult?>()
    val bidPlacementResultLiveData: LiveData<BidPlacementResult?> = _bidPlacementResultLiveData

    private val _userBidLiveData = MutableLiveData<TransactionBid?>()
    val userBidLiveData: LiveData<TransactionBid?> = _userBidLiveData

    private val _isLoadingLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData: LiveData<Boolean> = _isLoadingLiveData

    private val _callInitiationLiveData = MutableLiveData<InitiateCallResponse?>()
    val callInitiationLiveData: LiveData<InitiateCallResponse?> = _callInitiationLiveData

    private val _callInitiationErrorLiveData = MutableLiveData<String?>()
    val callInitiationErrorLiveData: LiveData<String?> = _callInitiationErrorLiveData

    private val _isCallLoadingLiveData = MutableLiveData<Boolean>()
    val isCallLoadingLiveData: LiveData<Boolean> = _isCallLoadingLiveData

    private val _isCallButtonActiveLiveData = MutableLiveData<Boolean>()
    val isCallButtonActiveLiveData: LiveData<Boolean> = _isCallButtonActiveLiveData

    private val _isBidPlacementLoadingLiveData = MutableLiveData<Boolean>()
    val isBidPlacementLoadingLiveData: LiveData<Boolean> = _isBidPlacementLoadingLiveData

    var transactionId: String = ""
    var userExistingBid: TransactionBid? = null
    private var isTransactionDetailsLoaded = false
    private var isUserBidsLoaded = false

    /**
     * Load bid details from API
     */
    fun loadBidDetails(bidId: String) {
        transactionId = bidId
        // Reset loading state
        isTransactionDetailsLoaded = false
        isUserBidsLoaded = false
        _isLoadingLiveData.postValue(true)
        
        compositeDisposable += transactionsRepository.transactionDetails(transactionId)
            .onBackground()
            .subscribe { response, error ->
                if (!error) {
                    _transactionLiveData.postValue(response)
                    // After loading transaction details, fetch user's bid status
                    fetchTransactionBids()
                } else {
                    error.handle()
                    _transactionLiveData.postValue(null)
                    // Still fetch bids even if transaction details fail
                    fetchTransactionBids()
                }
                isTransactionDetailsLoaded = true
                updateLoadingState()
            }
    }

    /**
     * Fetch transaction bids to check if user already has a bid
     */
    fun fetchTransactionBids() {
        compositeDisposable += bidsRepository.transactionBids(transactionId)
            .onBackground()
            .subscribe { _bRes, error ->
                if (!error) {
                    // Extract user's existing bid (if any)
                    val userBid = _bRes.first.first
                    userExistingBid = userBid
                    _userBidLiveData.postValue(userBid)
                    // Enable call button if user has placed a bid
                    _isCallButtonActiveLiveData.postValue(userBid != null)
                } else {
                    // If error or no bid found, set to null
                    userExistingBid = null
                    _userBidLiveData.postValue(null)
                    // Disable call button if no bid
                    _isCallButtonActiveLiveData.postValue(false)
                }
                isUserBidsLoaded = true
                updateLoadingState()
            }
    }

    /**
     * Update loading state - hide progress when all APIs are complete
     */
    private fun updateLoadingState() {
        if (isTransactionDetailsLoaded && isUserBidsLoaded) {
            _isLoadingLiveData.postValue(false)
        }
    }

    /**
     * Place or revise a bid with the specified amount
     * Automatically determines whether to create new bid or edit existing bid
     */
    fun placeBid(bidId: String, bidAmount: Int) {
        if (userExistingBid == null) {
            // No existing bid - Create new bid
            createNewBid(bidId, bidAmount)
        } else {
            // Existing bid found - Edit/Revise bid
            reviseBid(bidId, bidAmount)
        }
    }

    /**
     * Create a new bid
     */
    private fun createNewBid(bidId: String, bidAmount: Int) {
        _isBidPlacementLoadingLiveData.postValue(true)
        compositeDisposable += bidsRepository.createBid(
            isPMT = false,
            transactionId = bidId,
            amount = bidAmount,
            pmtRate = 0,
            commercialType = "FTL",
            expectedArrivalTimePickup = null,
            expectedArrivalTimePickupRemark = null,
            tentativeTripsCount = null,
            vehicleNumber = null,
            placementDays = null,
            demandType = "marketplace"
        )
            .onBackground()
            .progress()
            .subscribe { response, error ->
                _isBidPlacementLoadingLiveData.postValue(false)
                if (!error && response.isSuccess) {
                    // Enable call button immediately after successful bid placement
                    _isCallButtonActiveLiveData.postValue(true)
                    _bidPlacementResultLiveData.postValue(
                        BidPlacementResult(success = true, message = "Bid placed successfully")
                    )
                    // Refresh transaction details after placing bid
                    loadBidDetails(bidId)
                } else {
                    error.handle()
                    _bidPlacementResultLiveData.postValue(
                        BidPlacementResult(
                            success = false,
                            errorMessage = error.message ?: "Failed to place bid"
                        )
                    )
                }
            }
    }

    /**
     * Revise/Edit an existing bid
     */
    private fun reviseBid(bidId: String, bidAmount: Int) {
        val existingBidId = userExistingBid?.key() ?: run {
            _bidPlacementResultLiveData.postValue(
                BidPlacementResult(success = false, errorMessage = "Bid ID not found")
            )
            return
        }

        _isBidPlacementLoadingLiveData.postValue(true)
        compositeDisposable += bidsRepository.editBid(
            isPMT = false,
            transactionId = bidId,
            bidId = existingBidId,
            amount = bidAmount,
            commercialType = "FTL",
            pmtRate = 0,
            expectedArrivalTimePickup = null,
            expectedArrivalTimePickupRemark = null,
            tentativeTripsCount = null,
            vehicleNumber = null,
            placementDays = null
        )
            .onBackground()
            .progress()
            .subscribe { response, error ->
                _isBidPlacementLoadingLiveData.postValue(false)
                if (!error && response.isSuccess) {
                    // Keep call button enabled after successful bid revision
                    _isCallButtonActiveLiveData.postValue(true)
                    _bidPlacementResultLiveData.postValue(
                        BidPlacementResult(success = true, message = "Bid revised successfully")
                    )
                    // Refresh transaction details after revising bid
                    loadBidDetails(bidId)
                } else {
                    error.handle()
                    _bidPlacementResultLiveData.postValue(
                        BidPlacementResult(
                            success = false,
                            errorMessage = error.message ?: "Failed to revise bid"
                        )
                    )
                }
            }
    }

    /**
     * Initiate marketplace call using call masking API
     */
    fun initiateMarketplaceCall(transactionId: String, bidId: String) {
        Log.d("MarketPlaceBidDetailsViewModel", "==================== INITIATING CALL ====================")
        Log.d("MarketPlaceBidDetailsViewModel", "Transaction ID: $transactionId")
        Log.d("MarketPlaceBidDetailsViewModel", "Bid ID: $bidId")
        Log.d("MarketPlaceBidDetailsViewModel", "Source: marketplace")
        Log.d("MarketPlaceBidDetailsViewModel", "========================================================")
        
        // Set loading state to true
        _isCallLoadingLiveData.postValue(true)
        
        compositeDisposable += spotBiddingRepository.initiateMarketplaceCall(
            transactionId = transactionId,
            bidId = bidId,
            source = "marketplace"
        )
            .onBackground()
            .subscribe({ response ->
                Log.d("MarketPlaceBidDetailsViewModel", "==================== CALL INITIATION SUCCESS ====================")
                Log.d("MarketPlaceBidDetailsViewModel", "Response Success: ${response.success}")
                Log.d("MarketPlaceBidDetailsViewModel", "Bridge Numbers Count: ${response.data?.size ?: 0}")
                response.data?.forEachIndexed { index, bridgeData ->
                    Log.d("MarketPlaceBidDetailsViewModel", "Bridge #${index + 1}:")
                    Log.d("MarketPlaceBidDetailsViewModel", "  - Number: ${bridgeData.bridgeNumber}")
                    Log.d("MarketPlaceBidDetailsViewModel", "  - Vendor: ${bridgeData.vendor}")
                    Log.d("MarketPlaceBidDetailsViewModel", "  - Expiry: ${bridgeData.expiry}")
                }
                Log.d("MarketPlaceBidDetailsViewModel", "================================================================")
                
                // Set loading state to false
                _isCallLoadingLiveData.postValue(false)
                
                if (response.success && !response.data.isNullOrEmpty()) {
                    _callInitiationLiveData.postValue(response)
                } else {
                    Log.w("MarketPlaceBidDetailsViewModel", "Response success was false or data was empty")
                    _callInitiationErrorLiveData.postValue("Unable to initiate call. Please try again.")
                }
            }, { error ->
                // Log complete error details for debugging
                Log.e("MarketPlaceBidDetailsViewModel", "==================== CALL INITIATION ERROR ====================")
                Log.e("MarketPlaceBidDetailsViewModel", "Error Type: ${error.javaClass.name}")
                Log.e("MarketPlaceBidDetailsViewModel", "Error Message: ${error.message}")
                Log.e("MarketPlaceBidDetailsViewModel", "Error Cause: ${error.cause?.message}")
                Log.e("MarketPlaceBidDetailsViewModel", "Error Stack Trace:", error)
                
                // Log request details
                Log.e("MarketPlaceBidDetailsViewModel", "Request Details:")
                Log.e("MarketPlaceBidDetailsViewModel", "  - Transaction ID: $transactionId")
                Log.e("MarketPlaceBidDetailsViewModel", "  - Bid ID: $bidId")
                Log.e("MarketPlaceBidDetailsViewModel", "  - Source: marketplace")
                Log.e("MarketPlaceBidDetailsViewModel", "===============================================================")
                
                // Provide more specific error messages based on error type
                val errorMessage = when {
                    error is java.net.UnknownHostException -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "DNS Error: Unable to resolve hostname - ${error.message}")
                        "Network error: Unable to reach server. Please check your VPN connection and internet connectivity."
                    }
                    error is java.net.SocketTimeoutException -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "Timeout Error: ${error.message}")
                        "Request timed out. Please check your internet connection and try again."
                    }
                    error is java.net.ConnectException -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "Connection Error: ${error.message}")
                        "Unable to connect to server. Please check your network connection."
                    }
                    error is java.io.IOException -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "IO Error: ${error.message}")
                        "Network error: ${error.message ?: "Unable to connect to server"}"
                    }
                    error is retrofit2.HttpException -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "HTTP Error: Code=${error.code()}, Message=${error.message()}")
                        "Server error: ${error.message()}"
                    }
                    else -> {
                        Log.e("MarketPlaceBidDetailsViewModel", "Unknown Error: ${error.javaClass.name} - ${error.message}")
                        error.message ?: "Failed to initiate call. Please check your connection."
                    }
                }
                
                // Set loading state to false
                _isCallLoadingLiveData.postValue(false)
                
                _callInitiationErrorLiveData.postValue(errorMessage)
            })
    }
}

/**
 * Data class for bid placement result
 */
data class BidPlacementResult(
    val success: Boolean,
    val message: String? = null,
    val errorMessage: String? = null
)


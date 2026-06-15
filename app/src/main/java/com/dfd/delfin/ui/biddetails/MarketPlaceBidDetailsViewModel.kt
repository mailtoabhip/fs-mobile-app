package com.dfd.delfin.ui.biddetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.SpotBiddingRepository
import com.dfd.delfin.api.repository.TransactionsRepository
import com.dfd.delfin.api.response.InitiateCallResponse
import com.dfd.delfin.data.bids.TransactionBid
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * ViewModel for MarketPlace Bid Details screen
 */
class MarketPlaceBidDetailsViewModel @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val spotBiddingRepository: SpotBiddingRepository
) : BaseViewModel() {

    private val _transactionLiveData = MutableLiveData<HomeBidsRequestItemData?>()
    val transactionLiveData: LiveData<HomeBidsRequestItemData?> = _transactionLiveData

    private val _bidPlacementResultLiveData = MutableLiveData<BidPlacementResult?>()
    val bidPlacementResultLiveData: LiveData<BidPlacementResult?> = _bidPlacementResultLiveData

    private val _userBidLiveData = MutableLiveData<TransactionBid?>()
    val userBidLiveData: LiveData<TransactionBid?> = _userBidLiveData

    private val _bidStatusLiveData = MutableLiveData<MarketplaceBidStatus>()
    val bidStatusLiveData: LiveData<MarketplaceBidStatus> = _bidStatusLiveData

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
    var demandType : String = ""
    var userExistingBid: TransactionBid? = null
    private var isTransactionDetailsLoaded = false
    private var isUserBidsLoaded = false
    private var bidEndTime: Date? = null

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
                    demandType = response.demandType?:""
                    _transactionLiveData.postValue(response)
                    bidEndTime = response?.contractBiddingEndTime?.let { endTimeStr ->
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            sdf.timeZone = TimeZone.getTimeZone("IST")  // Set timezone to IST to match server response
                            sdf.parse(endTimeStr)
                        } catch (e: Exception) {
                            Log.e("BidDetails", "Failed to parse bid end time: $endTimeStr", e)
                            null
                        }
                    }
                    //fetchTransactionBids()
                } else {
                    error.handle()
                    bidEndTime = null
                    _transactionLiveData.postValue(null)
                    // Still fetch bids even if transaction details fail
                    //fetchTransactionBids()
                }
                isTransactionDetailsLoaded = true
                updateLoadingState()
            }
    }

    /**
     * Determine bid status based on transaction bids API response
     */
    private fun determineBidStatus(userBid: TransactionBid?, acceptedBid: TransactionBid?) {
        // User hasn't placed any bid
        if (userBid == null) {
            _bidStatusLiveData.postValue(MarketplaceBidStatus.NoBid)
            return
        }
        
        // Check bid status from API
        when (userBid._status) {
            "accepted" -> {
                // User's bid was accepted/confirmed
                _bidStatusLiveData.postValue(
                    MarketplaceBidStatus.Confirmed(bidAmount = userBid.bidAmount)
                )
            }
            "rejected" -> {
                // User's bid was rejected/lost
                _bidStatusLiveData.postValue(
                    MarketplaceBidStatus.Rejected(winningBidAmount = acceptedBid?.bidAmount)
                )
            }
            "cancelled" -> {
                // Load was cancelled
                _bidStatusLiveData.postValue(MarketplaceBidStatus.Cancelled)
            }
            "open" -> {
                // Bid is still open/active
                val now = Date()
                val isBiddingOpen = bidEndTime?.after(now) ?: true

                if (isBiddingOpen) {
                    // Bidding is still open - user can revise
                    _bidStatusLiveData.postValue(
                        MarketplaceBidStatus.Active(bidAmount = userBid.bidAmount)
                    )
                } else {
                    // Bidding has closed, awaiting result
                    _bidStatusLiveData.postValue(
                        MarketplaceBidStatus.AwaitingResult(bidAmount = userBid.bidAmount)
                    )
                }
            }
            else -> {
                // Default to active state
                _bidStatusLiveData.postValue(
                    MarketplaceBidStatus.Active(bidAmount = userBid.bidAmount)
                )
            }
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
     * Initiate marketplace call using call masking API
     */
    fun initiateMarketplaceCall(transactionId: String, bidId: String) {
        
        // Set loading state to true
        _isCallLoadingLiveData.postValue(true)
        
        compositeDisposable += spotBiddingRepository.initiateMarketplaceCall(
            transactionId = transactionId,
            bidId = bidId,
            source = "marketplace"
        )
            .onBackground()
            .subscribe({ response ->
                response.data?.forEachIndexed { index, bridgeData ->
                }
                
                // Set loading state to false
                _isCallLoadingLiveData.postValue(false)
                
                if (response.success && !response.data.isNullOrEmpty()) {
                    _callInitiationLiveData.postValue(response)
                } else {
                    _callInitiationErrorLiveData.postValue("Unable to initiate call. Please try again.")
                }
            }, { error ->
                
                // Provide more specific error messages based on error type
                val errorMessage = when {
                    error is java.net.UnknownHostException -> {
                        "Network error: Unable to reach server. Please check your VPN connection and internet connectivity."
                    }
                    error is java.net.SocketTimeoutException -> {
                        "Request timed out. Please check your internet connection and try again."
                    }
                    error is java.net.ConnectException -> {
                        "Unable to connect to server. Please check your network connection."
                    }
                    error is java.io.IOException -> {
                        "Network error: ${error.message ?: "Unable to connect to server"}"
                    }
                    error is retrofit2.HttpException -> {
                        "Server error: ${error.message()}"
                    }
                    else -> {
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

/**
 * Sealed class representing different marketplace bid states
 * Based on transaction bids API response
 */
sealed class MarketplaceBidStatus {
    object NoBid : MarketplaceBidStatus()

    data class Active(val bidAmount: Double) : MarketplaceBidStatus()

    data class AwaitingResult(val bidAmount: Double) : MarketplaceBidStatus()

    data class Confirmed(val bidAmount: Double) : MarketplaceBidStatus()

    data class Rejected(val winningBidAmount: Double?) : MarketplaceBidStatus()

    object Cancelled : MarketplaceBidStatus()
}


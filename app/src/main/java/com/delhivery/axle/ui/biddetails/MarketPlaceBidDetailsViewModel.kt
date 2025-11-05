package com.delhivery.axle.ui.biddetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
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
    private val bidsRepository: BidsRepository
) : BaseViewModel() {

    private val _transactionLiveData = MutableLiveData<HomeBidsRequestItemData?>()
    val transactionLiveData: LiveData<HomeBidsRequestItemData?> = _transactionLiveData

    private val _bidPlacementResultLiveData = MutableLiveData<BidPlacementResult?>()
    val bidPlacementResultLiveData: LiveData<BidPlacementResult?> = _bidPlacementResultLiveData

    private val _userBidLiveData = MutableLiveData<TransactionBid?>()
    val userBidLiveData: LiveData<TransactionBid?> = _userBidLiveData

    private val _isLoadingLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData: LiveData<Boolean> = _isLoadingLiveData

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
                } else {
                    // If error or no bid found, set to null
                    userExistingBid = null
                    _userBidLiveData.postValue(null)
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
                if (!error && response.isSuccess) {
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
                if (!error && response.isSuccess) {
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
}

/**
 * Data class for bid placement result
 */
data class BidPlacementResult(
    val success: Boolean,
    val message: String? = null,
    val errorMessage: String? = null
)


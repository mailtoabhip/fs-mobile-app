package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.CreateTransactionBidRequest
import com.delhivery.axle.api.request.UpdateTransactionBidRequest
import com.delhivery.axle.api.response.BidSummaryResponse
import com.delhivery.axle.api.service.BidService
import com.delhivery.axle.data.bids.*
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.annotations.Since
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BidsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val bidService: BidService,
  private val userPrefs: UserPrefs
) : BaseRepository() {

  /**
   * Transaction Bids along with user bid and bid count
   *
   * [Triple] with userBid, all bids, count
   */
  fun transactionBids(transactionId: String) = bidService.transactionBids(transactionId)
      .convertResponse()
      .map {
        val userId = userRepository.userId()
        var hasPMT = false
        var hasFTL = false
        val userBid :TransactionBid?
        it.bids.forEach { it1 ->
          when (it1.biddingType.toLowerCase()) {
            "pmt" -> hasPMT = true
            "ftl" -> hasFTL = true
          }
        }
        var lowestBid: TransactionBid? = null
        if ((hasPMT && !hasFTL) || (!hasPMT && hasFTL)) {
          lowestBid = (it.bids.minBy { b -> b.bidAmount })
        }
        userBid = if (userPrefs.isParent) {
          it.bids.firstOrNull { _b -> _b.supplierId.safeEquals(userId) }
        } else {
          it.bids.firstOrNull { _b -> _b.secondaryVendorId.safeEquals(userId) }
        }
        Triple(
            Pair(userBid, lowestBid), it.bids, it.totalBids
        )
      }!!

  /**
   * Add/Update bid for loads
   */
  fun transactionBid(transactionId: String) = bidService.transactionBids(transactionId)
      .convertResponse()
      .map {
        val userId = userRepository.userId()
        val userBid :TransactionBid?
        userBid = if (userPrefs.isParent) {
          it.bids.firstOrNull { _b -> _b.supplierId.safeEquals(userId) }
        } else {
          it.bids.firstOrNull { _b -> _b.secondaryVendorId.safeEquals(userId) }
        }
        userBid
      }!!

  /**
   * Bulk call to fetch bids
   */
  fun bidsForLoads(
    transactions: List<HomeBidsRequestItemData>
  ) = bidService.bidsForLoads(
      userRepository.userId(),
      transactions.map { it.transactionId }.joinToString(",") { it.toString() }
  )
      .convertResponse()
      .map {
        Pair(transactions, it.bids)
      }!!

    /**
     * Bulk call to fetch bids
     */
    fun bidsForBulkLoads(
        bids: List<TransactionBid>
    ) = bidService.bidsForLoads(
        userRepository.userId(),
        bids.map { it.transactionId }.joinToString(",") { it.toString() }
    )
        .convertResponse()
        .map {
            Pair(bids, it.bids)
        }!!

    /**
   * Create Bid
   */
  fun createBid(
    isPMT: Boolean,
    transactionId: String,
    amount: Int,
    pmtRate: Int,
    commercialType: String
  ) = CreateTransactionBidRequest.getRequest(
      isPMT, transactionId, userRepository.userId(),
      "${userPrefs.userName} ${userPrefs.pancard}",
      amount, pmtRate, commercialType, userPrefs.isTestUser
  ).let { bidService.createTransactionBid(it) }

  /**
   * Edit bid
   */
  fun editBid(
    isPMT: Boolean,
    transactionId: String,
    bidId: String,
    amount: Int,
    commercialType: String,
    pmtRate: Int
  ) = UpdateTransactionBidRequest.getRequest(
      isPMT, transactionId, bidId, amount, userRepository.userId(), pmtRate, commercialType
  )
      .let { bidService.updateTransactionBid(it) }



    /**
     * Edit bulk bid
     */
  fun editBulkBid(
    bulkBidModifyRequest: BulkBidUpdateRequest
  ) = bidService.updateBulkTransactionBids(bulkBidModifyRequest).convertResponse()

    fun removeBulkBids(
            bulkBidRemoveRequest: BulkBidRemoveRequest
    )= bidService.removeBulkTransactionBids(bulkBidRemoveRequest).convertResponse()

    /**
     * Create Bulk Bids
     */
  fun createBulkBids(
      bulkBidCreateRequest: BulkBidCreateRequest
  ) = bidService.createBulkTransactionBids(bulkBidCreateRequest).convertResponse()

  /**
   * User/supplier bids by status as [Pair] of Total bids count and List of [TransactionBid]
   */
  fun userBidsByStatus(
    status: TransactionBidStatus,
    offset: Int
  ) = bidService.userBidsByStatus(
      userRepository.userId(), offset, UserBidsLoadLimit, status.statusKey
  )
      .convertResponse()
      .map { Pair(it.totalBids, it.bids) }

  /**
   * User/supplier bids by status as [Pair] of Total bids count and List of [TransactionBid]
   */
  fun userBids(
    offset: Int,
    statuses: String
  ) = bidService.bidsForStatuses(userRepository.userId(),
      UserBidsLoadLimit, offset, statuses)
      .convertResponse()
      .map { Pair(it.totalBids, it.bids) }

  /**
   * User/supplier bid summary [BidSummaryResponse]
   */
  fun userBidsSummary() = bidService.userBidsSummary(userRepository.userId()).convertResponse()

  /**
   * Get lowest bid for transactionIds
   */
  fun bulkLowestBidsForTransactions(bids: List<TransactionBid>) =
    bids.joinToString(separator = ",") { it.transactionId }
        .let { bidService.bulkLowestBidsForTransactions(it) }
        .convertResponse()

  /**
   * Get lowest bid for loads
   */
  fun bulkLowestBidsForLoads(transactions: List<HomeBidsRequestItemData>) =
    bidService.bulkLowestBidsForTransactions(
        transactions.map { it.transactionId }.joinToString(",") { it.toString() }
    )
        .convertResponse()
        .map {
          Pair(transactions, it)
        }!!
}

/* User bids pagination load limit */
private const val UserBidsLoadLimit = 10
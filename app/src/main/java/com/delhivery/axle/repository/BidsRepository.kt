package com.delhivery.axle.repository

import com.delhivery.axle.api.BidService
import com.delhivery.axle.api.request.CreateTransactionBidRequest
import com.delhivery.axle.api.request.UpdateTransactionBidRequest
import com.delhivery.axle.api.response.BidSummaryResponse
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.safeEquals
import com.delhivery.axle.utils.prefs.UserPrefs
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
        val lowestBid = (it.bids.minBy { b -> b.bidAmount })
        val userBid = it.bids.firstOrNull { _b -> _b.supplierId.safeEquals(userId) }
        Triple(
            Pair(userBid, lowestBid), it.bids, it.totalBids
        )
      }!!

  fun transactionBid(transactionId: String) = bidService.transactionBids(transactionId)
      .convertResponse()
      .map {
        val userId = userRepository.userId()
        val userBid = it.bids.filter { _b -> _b.supplierId.safeEquals(userId) }
            .firstOrNull()
        userBid
      }!!

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
  ) = bidService.bidsForStatuses(userRepository.userId(), UserBidsLoadLimit, offset, statuses)
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
}

/* User bids pagination load limit */
private const val UserBidsLoadLimit = 10
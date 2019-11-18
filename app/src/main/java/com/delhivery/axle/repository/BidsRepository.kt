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

  private fun createList(
    t1: Pair<Int, List<TransactionBid>>,
    t2: Pair<Int, List<TransactionBid>>,
    t3: Pair<Int, List<TransactionBid>>
  ): Pair<Triple<Int, Int, Int>, MutableList<TransactionBid>> {
    val count = Triple(t1.first, t2.first, t3.first)
    val list: MutableList<TransactionBid> = mutableListOf()
    list.addAll(t1.second)
    list.addAll(t2.second)
    list.addAll(t3.second)
    return Pair(count, list)
  }

  /**
   * Transaction Bids along with user bid and bid count
   *
   * [Triple] with userBid, all bids, count
   */
  fun transactionBids(transactionId: String) = bidService.transactionBids(transactionId)
      .convertResponse()
      .map {
        val userId = userRepository.userId()
        val lowest = (it.bids.minBy { b -> b.bidAmount })?.bidAmount ?: 0.0
        val userBid = it.bids.filter { _b -> _b.supplierId.safeEquals(userId) }
            .firstOrNull()
        Triple(Pair(userBid, lowest), it.bids, it.totalBids)
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
    amount: Int
  ) = CreateTransactionBidRequest.getRequest(
      isPMT, transactionId, userRepository.userId(),
      "${userPrefs.userName} ${userPrefs.pancard}",
      amount, userPrefs.isTestUser
  ).let { bidService.createTransactionBid(it) }

  /**
   * Edit bid
   */
  fun editBid(
    transactionId: String,
    bidId: String,
    amount: Int
  ) = UpdateTransactionBidRequest(transactionId, bidId, amount, userRepository.userId())
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
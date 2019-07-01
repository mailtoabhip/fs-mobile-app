package com.delhivery.orion.repository

import com.delhivery.orion.api.BidService
import com.delhivery.orion.api.request.CreateTransactionBidRequest
import com.delhivery.orion.api.request.UpdateTransactionBidRequest
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.bids.TransactionBidStatus
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.safeEquals
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BidsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val bidService: BidService
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
        val userBid = it.bids.filter { _b -> _b.supplierId.safeEquals(userId) }
            .firstOrNull()
        Triple(userBid, it.bids, it.totalBids)
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
    transactionId: String,
    amount: Int
  ) = CreateTransactionBidRequest(
      transactionId, userRepository.userId(), userRepository.username(), amount
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
  ) = bidService.bidsForStatuses(
      userRepository.userId(), UserBidsLoadLimit,
      offset, statuses
  )
      .convertResponse()
      .map { Pair(it.totalBids, it.bids) }

  /**
   * User/supplier bid summary [BidSummaryResponse]
   */
  fun userBidsSummary(
  ) = bidService
      .userBidsSummary(userRepository.userId())
      .convertResponse()
}

/* User bids pagination load limit */
private const val UserBidsLoadLimit = 10
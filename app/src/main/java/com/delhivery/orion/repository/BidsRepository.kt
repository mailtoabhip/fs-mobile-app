package com.delhivery.orion.repository

import com.delhivery.orion.api.BidService
import com.delhivery.orion.api.request.CreateTransactionBidRequest
import com.delhivery.orion.api.request.UpdateTransactionBidRequest
import com.delhivery.orion.data.bids.TransactionBid
import com.delhivery.orion.data.bids.TransactionBidStatus
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.safeEquals
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BidsRepository @Inject constructor(
  private val userRepository: UserRepository,
  private val bidService: BidService
) : BaseRepository() {

  /**
   * Get user bids count, my bids and confirm bids
   */
  fun userBidsCount() =
    Single.zip(
        userBids(TransactionBidStatus.Open, 0),
        userBids(TransactionBidStatus.Accepted, 0),
        BiFunction<Pair<Int, List<TransactionBid>>, Pair<Int, List<TransactionBid>>, Pair<Int, Int>>
        { _myBids, _cnfBids ->
          Pair(_myBids.first, _cnfBids.first)
        })!!

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
   * User/supplier bids as [Pair] of Total bids count and List of [TransactionBid]
   */
  fun userBids(
    status: TransactionBidStatus,
    offset: Int
  ) = bidService.userBids(userRepository.userId(), offset, UserBidsLoadLimit, status.statusKey)
      .convertResponse()
      .map { Pair(it.totalBids, it.bids) }
}

/* User bids pagination load limit */
private const val UserBidsLoadLimit = 10
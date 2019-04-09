package com.delhivery.orion.repository

import com.delhivery.orion.api.BidService
import com.delhivery.orion.api.request.CreateTransactionBidRequest
import com.delhivery.orion.api.request.UpdateTransactionBidRequest
import com.delhivery.orion.utils.extensions.convertResponse
import com.delhivery.orion.utils.extensions.safeEquals
import io.reactivex.Single
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
  fun userBidsCount() = Single.just(Pair(5, 7))

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
      }

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
}
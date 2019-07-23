package com.delhivery.axle.repository

import com.delhivery.axle.api.TransactionService
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRepository @Inject constructor(
  private val transactionService: TransactionService,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs
) : BaseRepository() {

  /**
   * Get user transactions
   */
  fun fetchLoadBoardTransactions(offset: Int) = transactionService.loadBoardTransactions(
      userRepository.userId(), userPrefs.cityCode ?: "",
      offset, UserTripsLoadLimit
  ).convertResponse()

  /**
   * Search transactions
   */
  fun searchTransactions(
    offset: Int,
    source: String,
    destination: String?,
    truckType: String?
  ) = transactionService.transactions(
      offset, null, source, destination, truckType
  ).convertResponse()

  /**
   * Get bulk transactions using ids
   */
  fun bulkTransactions(bids: List<TransactionBid>) =
    bids.joinToString(
        separator = ","
    ) { it.transactionId }.let { transactionService.bulkTransactions(it) }
        .convertResponse()
        .map { Pair(bids, it) }

  /**
   * Transaction details
   */
  fun transactionDetails(id: String) = transactionService.transactionDetails(id).convertResponse()

  fun transactionTripMeter() =
    transactionService.transactionsTripMeter(userRepository.userId()).convertResponse()
}

enum class TransactionStatus(val statusId: String) {
  Requested("requested"),
  TruckConfirmed("truck_confirmed"),
  Completed("completed")
}
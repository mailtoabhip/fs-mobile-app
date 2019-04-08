package com.delhivery.orion.repository

import com.delhivery.orion.api.TransactionService
import com.delhivery.orion.utils.extensions.convertResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRepository @Inject constructor(
  private val transactionService: TransactionService
) : BaseRepository() {

  /**
   * Get user transactions
   */
  fun transactions(
    offset: Int,
    status: TransactionStatus
  ) = transactionService.transactions(offset, status.statusId).convertResponse()

  /**
   * Search transactions
   */
  fun searchTransactions(
    offset: Int,
    source: String,
    destination: String
  ) = transactionService.transactions(
      offset, null, source = source, destination = destination
  ).convertResponse()
}

enum class TransactionStatus(val statusId: String) {
  Requested("requested"),
  TruckConfirmed("truck_confirmed"),
  Completed("completed")
}
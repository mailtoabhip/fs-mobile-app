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
  fun transaction(offset: Int) = transactionService.transactions(offset).convertResponse()
}
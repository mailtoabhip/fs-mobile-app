package com.dfd.delfin.api.repository

import com.dfd.delfin.api.response.ExpenseData
import com.dfd.delfin.api.service.ExpenseService
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.utils.ErrorLogger
import com.dfd.delfin.utils.extensions.convertResponse
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
  private val expenseService: ExpenseService,
  errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

  /**
   * Get bulk expnse
   */
  fun bulkExpense(
    trips: List<HomeTripsItemData>
  ): Single<Pair<List<HomeTripsItemData>, List<ExpenseData>>> =
    expenseService.bulkExpenses(
        trips.map { it.transactionId }.joinToString(",") { it }
    )
        .convertResponse()
        .map { Pair(trips, it) }

}
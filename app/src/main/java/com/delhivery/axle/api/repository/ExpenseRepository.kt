package com.delhivery.axle.api.repository

import com.delhivery.axle.api.response.ExpenseData
import com.delhivery.axle.api.service.ExpenseService
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
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
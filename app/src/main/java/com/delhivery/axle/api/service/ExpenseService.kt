package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ExpenseData
import com.delhivery.axle.api.response.TripChargesResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Handle network calls to Expense Service
 */
interface ExpenseService {

  @GET("/api/v2/views/payments/summary")
  fun bulkExpenses(
    @Query("transaction_ids") transactionIds: String
  ): Single<BaseResponse<List<ExpenseData>>>

  /**
   * Get v2 charges
   */
  @GET("/api/v2/charges")
  fun charges(
    @Query("transaction_ids") transactionIds: String
  ): Single<BaseResponse<Map<String, List<TripChargesResponse>>>>

}
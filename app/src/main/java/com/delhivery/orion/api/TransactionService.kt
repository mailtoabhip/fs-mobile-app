package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TransactionsResponse
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionService {

  /**
   * List all transactions
   */
  @GET("/transactions/list/")
  fun transactions(
    @Query("offset") offset: Int,
    @Query("status") status: String?,
    @Query("source") source: String? = null,
    @Query("destination") destination: String? = null
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * Transaction details
   */
  @GET("/transactions/")
  fun transactionDetails(
    @Query("uuid") transactionId: String
  ): Single<BaseResponse<HomeBidsRequestItemData>>

  /**
   * Bulk transaction ids
   *
   * @param transactionIds Comma separated ids
   */
  @GET("/transactions/list/")
  fun bulkTransactions(
    @Query("transaction_ids") transactionIds: String
  ): Single<BaseResponse<TransactionsResponse>>
}
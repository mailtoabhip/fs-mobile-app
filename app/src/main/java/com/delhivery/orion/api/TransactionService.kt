package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TransactionsResponse
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionService {

  /**
   * List all transactions
   */
  @GET("/transactions/list/")
  fun transactions(
    @Query("offset") offset: Int,
    @Query("status") status: String?,
    @Query("origin_city_code") source: String? = null,
    @Query("destination_city_code") destination: String? = null,
    @Query("truck_type") truckType: String? = null
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * List all transactions
   */
  @GET("/transactions/loadboard/{supplier_id}")
  fun loadBoardTransactions(
    @Path("supplier_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int
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
    @Query("transactions_ids") transactionIds: String
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * List all transactions
   */
  @GET("/transactions/tripmeter/{supplier_id}")
  fun transactionsTripMeter(
    @Path("supplier_id") userId: String
  ): Single<BaseResponse<TransactionsResponse>>
}
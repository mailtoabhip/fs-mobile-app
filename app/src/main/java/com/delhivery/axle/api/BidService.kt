package com.delhivery.axle.api

import com.delhivery.axle.api.request.CreateTransactionBidRequest
import com.delhivery.axle.api.request.UpdateTransactionBidRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.BidSummaryResponse
import com.delhivery.axle.api.response.CreateTransactionBidResponse
import com.delhivery.axle.api.response.LowestBidResponse
import com.delhivery.axle.api.response.TransactionBidsResponseBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface BidService {

  /**
   * List transaction bids
   */
  @GET("bids")
  fun transactionBids(@Query("transaction_id") transactionId: String)
      : Single<BaseResponse<TransactionBidsResponseBody>>

  /**
   * Create new transaction Bid
   */
  @POST("bids/")
  fun createTransactionBid(@Body request: CreateTransactionBidRequest)
      : Single<BaseResponse<CreateTransactionBidResponse>>

  /**
   * Update bid amount
   */
  @PATCH("/bids/")
  fun updateTransactionBid(
    @Body request: UpdateTransactionBidRequest
  ): Single<BaseResponse<CreateTransactionBidResponse>>

  /**
   * Get User/supplier bids by status
   */
  @GET("bids")
  fun userBidsByStatus(
    @Query("supplier_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("bid_status") status: String
  ): Single<BaseResponse<TransactionBidsResponseBody>>

  /**
   * Get all User/supplier bids
   */
  @GET("bids")
  fun userAllBids(
    @Query("supplier_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int
  ): Single<BaseResponse<TransactionBidsResponseBody>>

  /**
   * Get all user bids for loads basis transaction ids
   */
  @GET("bids")
  fun bidsForLoads(
    @Query("supplier_id") userId: String,
    @Query("transaction_ids") transactionIds: String? = null
  ): Single<BaseResponse<TransactionBidsResponseBody>>

  /**
   * List of trips
   */
  @GET("bids/")
  fun bidsForStatuses(
    @Query("supplier_id") userId: String,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
    @Query("bid_statuses") status: String? = null
  ): Single<BaseResponse<TransactionBidsResponseBody>>

  /**
   * Get bids summary basis [userId]
   */
  @GET("/bids/summary")
  fun userBidsSummary(
    @Query("supplier_id") userId: String
  ): Single<BaseResponse<BidSummaryResponse>>

  /**
   * Get lowest bid for [transactionIds]
   */
  @GET("/bids/lowest")
  fun bulkLowestBidsForTransactions(
    @Query("transaction_id_list") transactionIds: String
  ): Single<BaseResponse<List<LowestBidResponse>>>
}
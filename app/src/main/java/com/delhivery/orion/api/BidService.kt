package com.delhivery.orion.api

import com.delhivery.orion.api.request.CreateTransactionBidRequest
import com.delhivery.orion.api.request.UpdateTransactionBidRequest
import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.CreateTransactionBidResponse
import com.delhivery.orion.api.response.TransactionBidsResponseBody
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
}
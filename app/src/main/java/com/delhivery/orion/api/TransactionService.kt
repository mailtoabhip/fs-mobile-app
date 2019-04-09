package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TransactionsResponse
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionService {

  @GET("/transactions/list/")
  fun transactions(
    @Query("offset") offset: Int,
    @Query("status") status: String?,
    @Query("source") source: String? = null,
    @Query("destination") destination: String? = null
  ): Single<BaseResponse<TransactionsResponse>>

  @GET("/transactions/")
  fun transactionDetails(
    @Query("uuid") transactionId: String
  ): Single<BaseResponse<HomeBidsRequestItemData>>
}
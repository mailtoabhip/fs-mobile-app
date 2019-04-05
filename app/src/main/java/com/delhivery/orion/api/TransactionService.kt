package com.delhivery.orion.api

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.api.response.TransactionsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionService {

  @GET("/transactions/list/")
  fun transactions(
    @Query("offset") offset: Int,
    @Query("status") status: String = "requested"
  ): Single<BaseResponse<TransactionsResponse>>
}
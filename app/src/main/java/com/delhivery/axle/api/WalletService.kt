package com.delhivery.axle.api

import com.delhivery.axle.api.request.WalletUpdateRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.WalletDataResponse
import com.delhivery.axle.api.response.WalletTransactionsResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface WalletService {

  /**
   * Activate Wallet
   */
  @PATCH("/api/v1/wallet/{wallet_id}")
  fun activateWallet(
    @Path("wallet_id") walletId: String,
    @Body payload: WalletUpdateRequest = WalletUpdateRequest(false)
  ): Single<BaseResponse<WalletDataResponse>>

  /**
   * Get wallet data
   */
  @GET("/api/v1/wallet/{wallet_id}")
  fun fetchWalletData(
    @Path("wallet_id") walletId: String
  ): Single<BaseResponse<WalletDataResponse>>

  /**
   * Get wallet data
   */
  @GET("/api/v1/wallet/{wallet_id}/transactions/")
  fun fetchWalletTransactions(
    @Path("wallet_id") walletId: String
  ): Single<BaseResponse<WalletTransactionsResponse>>

}
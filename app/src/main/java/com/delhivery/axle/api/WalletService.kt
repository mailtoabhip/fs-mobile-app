package com.delhivery.axle.api

import com.delhivery.axle.api.request.BankTransferRequest
import com.delhivery.axle.api.request.WalletUpdateRequest
import com.delhivery.axle.api.response.BankTransferResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FuelCardsResponse
import com.delhivery.axle.api.response.WalletDataResponse
import com.delhivery.axle.api.response.WalletTransactionsResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletService {

  /**
   * Activate Wallet
   */
  @PATCH("/api/v1/wallet/{wallet_id}")
  fun activateWallet(
    @Path("wallet_id") walletId: String,
    @Body payload: WalletUpdateRequest
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
    @Path("wallet_id") walletId: String,
    @Query("start_time") startTime: String = "2018-09-18T13:16:44",
    @Query("end_time") endTime: String = "2020-09-18T13:16:44"
  ): Single<BaseResponse<WalletTransactionsResponse>>

  /**
   * Tranfer amount from wallet to bank
   */
  @POST("/api/v1/wallet/{wallet_id}/transactions/")
  fun transferToBank(
    @Path("wallet_id") walletId: String,
    @Body payload: BankTransferRequest
  ): Single<BaseResponse<BankTransferResponse>>

  /**
   * Fetch active fuel cards
   */
  @GET("iocl/get_cards")
  fun fetchActiveFuelCards(): Single<BaseResponse<FuelCardsResponse>>

}
package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.BankTransferRequest
import com.dfd.delfin.api.request.CreateFuelCardRequest
import com.dfd.delfin.api.request.WalletUpdateRequest
import com.dfd.delfin.api.response.BankTransferResponse
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.WalletDataResponse
import com.dfd.delfin.api.response.WalletTransactionsResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Handle network calls to Wallet Service
 */
interface WalletService {

  /**
   * Activate Wallet
   */
  @PATCH("/api/v1/wallet/me")
  fun activateWallet(
    @Header("x-user-id") userId: String,
    @Body payload: WalletUpdateRequest
  ): Single<BaseResponse<WalletDataResponse>>

  /**
   * Get wallet data
   */
  @GET("/api/v1/wallet/me")
  fun fetchWalletData(
    @Header("x-user-id") userId: String
  ): Single<BaseResponse<WalletDataResponse>>

  /**
   * Get wallet data
   */
  @GET("/api/v1/wallet/me/transactions/")
  fun fetchWalletTransactions(
    @Header("x-user-id") userId: String,
    @Query("start_time") startTime: String = "2018-09-18T13:16:44",
    @Query("end_time") endTime: String = "2020-09-18T13:16:44"
  ): Single<BaseResponse<WalletTransactionsResponse>>

  /**
   * Tranfer amount from wallet to bank
   */
  @POST("/api/v1/wallet/me/transactions/")
  fun transferToBank(
    @Header("x-user-id") userId: String,
    @Body payload: BankTransferRequest
  ): Single<BaseResponse<BankTransferResponse>>

  /**
   * Fetch active fuel cards
   */
  @POST("/api/v1/wallet/me/transactions/")
  fun createFuelCard(
    @Header("x-user-id") userId: String,
    @Body payload: CreateFuelCardRequest
  ): Single<BaseResponse<BankTransferResponse>>

}
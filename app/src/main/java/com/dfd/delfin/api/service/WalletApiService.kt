package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.RechargeStatusResponse
import com.dfd.delfin.api.response.UserWalletResponse
import com.dfd.delfin.api.response.WalletRechargeHistoryResponse
import com.dfd.delfin.api.response.WalletRechargeInitResponse
import com.dfd.delfin.api.response.WalletTransactionHistoryResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Dedicated Wallet API Service
 *
 * Replaces wallet-related endpoints previously scattered across LoadBoardService.
 * All endpoints follow the new Wallet Service contract (/api/v1/wallet).
 * Base URL: LoadboardService (where /api/v1/wallet endpoints are hosted).
 *
 * Note: X-User-Id header is added globally via DelhiveryNetworkInterceptor.
 * TODO Remove All the User_Id headers from the API.
 */
interface WalletApiService {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Create Wallet
    // Replaces: POST /finance/users/wallet/
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create a new wallet for the user.
     *
     * Request body: { user_type, email, phone }
     * X-User-Id header acts as idempotency key.
     *
     * @return wallet details on success, or existing wallet on 409
     */
    @POST("/api/v1/wallet")
    fun createWallet(
        @Body request: JsonObject
    ): Single<BaseResponse<UserWalletResponse>>

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Recharge Wallet
    // Replaces: POST /finance/users/wallet/recharge
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Initiate a wallet recharge via payment gateway.
     *
     * Request body: { amount, redirect_url, cl_request_id }
     *
     * @return payment link and recharge details
     */
    @POST("/api/v1/wallet/recharge")
    fun rechargeWallet(
        @Body request: JsonObject
    ): Single<BaseResponse<WalletRechargeInitResponse>>

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Fetch Wallet Info
    // Replaces: GET /finance/users/wallet/
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get current wallet details (balance, thresholds, status).
     *
     * @return wallet info
     */
    @GET("/api/v1/wallet")
    fun fetchWalletInfo(
    ): Single<BaseResponse<UserWalletResponse>>

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Transactions History
    // Replaces: GET /finance/users/wallet/transactions/list
    //           GET /finance/users/wallet/transactions (single txn status)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetch paginated transaction history with optional filters.
     *
     * Pagination is cursor-based (pass next_cursor from previous response).
     * Date range must not exceed 90 days.
     * Pass txn_id to fetch a single transaction's status.
     *
     * @param start Start datetime (format: yyyy-MM-ddTHH:mm:ss)
     * @param end End datetime (format: yyyy-MM-ddTHH:mm:ss)
     * @param txnId Filter by specific transaction ID
     * @param type Filter by transaction type (debit/credit)
     * @param status Filter by transaction status
     * @param limit Max records to return (default 10)
     * @param cursor Cursor for next page (created_at of last item)
     */
    @GET("/api/v1/wallet/transactions")
    fun fetchTransactions(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("txn_id") txnId: String? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Single<BaseResponse<WalletTransactionHistoryResponse>>

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Recharge History
    // Replaces: GET /finance/users/wallet/recharge/transactions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetch paginated recharge history.
     *
     * start and end are required. Date range must not exceed 90 days.
     * Pagination is cursor-based.
     *
     * @param start Start datetime (required, format: yyyy-MM-ddTHH:mm:ss)
     * @param end End datetime (required, format: yyyy-MM-ddTHH:mm:ss)
     * @param rechargeId Filter by specific recharge ID
     * @param limit Max records to return
     * @param cursor Cursor for next page (created_at of last item)
     */
    @GET("/api/v1/wallet/recharges")
    fun fetchRechargeHistory(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("recharge_id") rechargeId: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): Single<BaseResponse<WalletRechargeHistoryResponse>>

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Fetch Recharge Status
    // Replaces: GET /finance/users/wallet/recharge (was GET, now POST)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get the latest status of a recharge.
     *
     * Request body: { recharge_id }
     *
     * @return recharge details with current status
     */
    @POST("/api/v1/wallet/recharge-status")
    fun fetchRechargeStatus(
        @Body request: JsonObject
    ): Single<BaseResponse<RechargeStatusResponse>>
}

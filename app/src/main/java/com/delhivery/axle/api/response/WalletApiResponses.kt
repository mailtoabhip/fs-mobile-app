package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Response for Recharge Wallet (API #2) — initiation
 * Replaces: WalletRechargeData (which only had plod_details.link and recharge_id)
 */
data class WalletRechargeInitResponse(
    @SerializedName("recharge_id") val rechargeId: String = "",
    @SerializedName("cl_request_id") val clRequestId: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("payment_link_url") val paymentLinkUrl: String = "",
    @SerializedName("ps_order_id") val psOrderId: String = "",
    @SerializedName("ps_txn_id") val psTxnId: String = ""
) : Serializable

/**
 * Response for Transactions History (API #6)
 * Replaces: WalletTransactionListResponse (which used offset-based pagination)
 */
data class WalletTransactionHistoryResponse(
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("total") val total: Int = 0,
    @SerializedName("total_amount") val totalAmount: String = "0.00",
    @SerializedName("has_next") val hasNext: Boolean = false,
    @SerializedName("next_cursor") val nextCursor: String? = null,
    @SerializedName("transactions") val transactions: List<WalletTransactionItemV2> = emptyList(),
    @SerializedName("txn_remarks") val txnRemarks: String = ""
)

/**
 * Individual transaction item from Transactions History (API #6)
 * Replaces: WalletTransactionItem (which used ref_type instead of txn_reason)
 */
data class WalletTransactionItemV2(
    @SerializedName("transaction_id") val transactionId: String = "",
    @SerializedName("transaction_type") val transactionType: String = "",
    @SerializedName("amount") val amount: String = "0.00",
    @SerializedName("status") val status: String = "",
    @SerializedName("updated_wallet_balance") val updatedWalletBalance: String = "0.00",
    @SerializedName("txn_reason") val txnReason: String = "",
    @SerializedName("txn_remarks") val txnRemarks: String = "",
    @SerializedName("created_at") val createdAt: String = ""
) : Serializable

/**
 * Response for Recharge History (API #7)
 * Replaces: WalletRechargeListResponse (which used offset-based pagination)
 */
data class WalletRechargeHistoryResponse(
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("total") val total: Int = 0,
    @SerializedName("total_amount") val totalAmount: String = "0.00",
    @SerializedName("opening_balance") val openingBalance: String = "0.00",
    @SerializedName("has_next") val hasNext: Boolean = false,
    @SerializedName("next_cursor") val nextCursor: String? = null,
    @SerializedName("recharges") val recharges: List<WalletRechargeItemV2> = emptyList(),
    @SerializedName("bank_reference_no") val bankReferenceNo: String = "",
    @SerializedName("payment_method") val paymentMethod: String = ""
)

/**
 * Individual recharge item from Recharge History (API #7)
 * Replaces: WalletRechargeItem
 */
data class WalletRechargeItemV2(
    @SerializedName("recharge_id") val rechargeId: String = "",
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("amount") val amount: String = "0.00",
    @SerializedName("status") val status: String = "",
    @SerializedName("payment_gateway") val paymentGateway: String = "",
    @SerializedName("pg_transaction_id") val pgTransactionId: String? = null,
    @SerializedName("updated_wallet_balance") val updatedWalletBalance: String = "0.00",
    @SerializedName("created_by") val createdBy: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("recharge_date") val rechargeDate: String? = null
) : Serializable

/**
 * Response for Fetch Recharge Status (API #10)
 * Replaces: WalletRechargeStatusResponse (which only had recharge_id, amount, status)
 */
data class RechargeStatusResponse(
    @SerializedName("recharge_id") val rechargeId: String = "",
    @SerializedName("wallet_id") val walletId: String = "",
    @SerializedName("amount") val amount: String = "0.00",
    @SerializedName("status") val status: String = "",
    @SerializedName("payment_gateway") val paymentGateway: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("recharge_date") val rechargeDate: String? = null,
    @SerializedName("updated_wallet_balance") val updatedWalletBalance: String = "0.00",
    @SerializedName("details") val details: RechargePaymentDetails? = null
) : Serializable

/**
 * Payment gateway details within RechargeStatusResponse
 */
data class RechargePaymentDetails(
    @SerializedName("pg_transaction_id") val pgTransactionId: String? = null
) : Serializable

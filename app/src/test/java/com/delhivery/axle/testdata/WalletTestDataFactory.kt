package com.delhivery.axle.testdata

import com.delhivery.axle.api.response.RechargePaymentDetails
import com.delhivery.axle.api.response.RechargeStatusResponse
import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.response.WalletRechargeHistoryResponse
import com.delhivery.axle.api.response.WalletRechargeInitResponse
import com.delhivery.axle.api.response.WalletRechargeItemV2
import com.delhivery.axle.api.response.WalletTransactionHistoryResponse
import com.delhivery.axle.api.response.WalletTransactionItemV2

/**
 * Factory for creating test data objects for Wallet-related tests.
 * Provides sensible defaults that can be overridden as needed.
 */
object WalletTestDataFactory {

    // ==================== UserWalletResponse ====================

    fun createUserWalletResponse(
        walletId: String = "WALLET456",
        userId: String = "USER789",
        currentBalance: String = "5000.00",
        minThreshold: String = "500.00",
        maxThreshold: String = "100000.00",
        lockedAmount: String = "0.00",
        isActive: Boolean = true,
        email: String = "test@example.com",
        phone: String = "9876543210",
        createdAt: String = "2024-01-01T10:00:00",
        updatedAt: String = "2024-01-15T14:30:00"
    ): UserWalletResponse = UserWalletResponse(
        walletId = walletId,
        userId = userId,
        currentBalance = currentBalance,
        minThreshold = minThreshold,
        maxThreshold = maxThreshold,
        lockedAmount = lockedAmount,
        isActive = isActive,
        email = email,
        phone = phone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun createEmptyWalletResponse(): UserWalletResponse = createUserWalletResponse(
        currentBalance = "0.00",
        isActive = false
    )

    // ==================== WalletTransactionHistoryResponse ====================

    fun createTransactionHistoryResponse(
        walletId: String = "WALLET456",
        total: Int = 2,
        totalAmount: String = "1500.00",
        hasNext: Boolean = false,
        nextCursor: String? = null,
        transactions: List<WalletTransactionItemV2> = listOf(
            createTransactionItem(),
            createTransactionItem(
                transactionId = "TXN002",
                transactionType = "debit",
                amount = "500.00",
                status = "success",
                txnReason = "ORDER_PAYMENT"
            )
        )
    ): WalletTransactionHistoryResponse = WalletTransactionHistoryResponse(
        walletId = walletId,
        total = total,
        totalAmount = totalAmount,
        hasNext = hasNext,
        nextCursor = nextCursor,
        transactions = transactions
    )

    fun createPaginatedTransactionResponse(
        page: Int = 1,
        hasNext: Boolean = true,
        nextCursor: String = "2024-01-10T10:00:00"
    ): WalletTransactionHistoryResponse = createTransactionHistoryResponse(
        total = 20,
        hasNext = hasNext,
        nextCursor = nextCursor,
        transactions = (1..10).map { i ->
            createTransactionItem(
                transactionId = "TXN_P${page}_$i",
                createdAt = "2024-01-${10 + i}T10:00:00"
            )
        }
    )

    fun createEmptyTransactionResponse(): WalletTransactionHistoryResponse =
        createTransactionHistoryResponse(
            total = 0,
            totalAmount = "0.00",
            transactions = emptyList()
        )

    // ==================== WalletTransactionItemV2 ====================

    fun createTransactionItem(
        transactionId: String = "TXN001",
        transactionType: String = "credit",
        amount: String = "1000.00",
        status: String = "success",
        updatedWalletBalance: String = "6000.00",
        txnReason: String = "WALLET_RECHARGE",
        txnRemarks: String = "Recharge via UPI",
        createdAt: String = "2024-01-15T14:30:00"
    ): WalletTransactionItemV2 = WalletTransactionItemV2(
        transactionId = transactionId,
        transactionType = transactionType,
        amount = amount,
        status = status,
        updatedWalletBalance = updatedWalletBalance,
        txnReason = txnReason,
        txnRemarks = txnRemarks,
        createdAt = createdAt
    )

    fun createPendingTransaction(
        transactionId: String = "TXN_PENDING"
    ): WalletTransactionItemV2 = createTransactionItem(
        transactionId = transactionId,
        status = "pending",
        txnReason = "ORDER_PAYMENT"
    )

    // ==================== WalletRechargeHistoryResponse ====================

    fun createRechargeHistoryResponse(
        walletId: String = "WALLET456",
        total: Int = 2,
        totalAmount: String = "3000.00",
        openingBalance: String = "2000.00",
        hasNext: Boolean = false,
        nextCursor: String? = null,
        recharges: List<WalletRechargeItemV2> = listOf(
            createRechargeItem(),
            createRechargeItem(
                rechargeId = "RCH002",
                amount = "1000.00",
                status = "pending"
            )
        )
    ): WalletRechargeHistoryResponse = WalletRechargeHistoryResponse(
        walletId = walletId,
        total = total,
        totalAmount = totalAmount,
        openingBalance = openingBalance,
        hasNext = hasNext,
        nextCursor = nextCursor,
        recharges = recharges
    )

    fun createPaginatedRechargeResponse(
        page: Int = 1,
        hasNext: Boolean = true,
        nextCursor: String = "2024-01-10T10:00:00"
    ): WalletRechargeHistoryResponse = createRechargeHistoryResponse(
        total = 20,
        hasNext = hasNext,
        nextCursor = nextCursor,
        recharges = (1..10).map { i ->
            createRechargeItem(
                rechargeId = "RCH_P${page}_$i",
                createdAt = "2024-01-${10 + i}T10:00:00"
            )
        }
    )

    fun createEmptyRechargeResponse(): WalletRechargeHistoryResponse =
        createRechargeHistoryResponse(
            total = 0,
            totalAmount = "0.00",
            recharges = emptyList()
        )

    // ==================== WalletRechargeItemV2 ====================

    fun createRechargeItem(
        rechargeId: String = "RCH001",
        walletId: String = "WALLET456",
        amount: String = "2000.00",
        status: String = "success",
        paymentGateway: String = "razorpay",
        pgTransactionId: String? = "PG_TXN_001",
        updatedWalletBalance: String = "7000.00",
        createdBy: String = "USER789",
        createdAt: String = "2024-01-15T14:30:00",
        rechargeDate: String? = "2024-01-15"
    ): WalletRechargeItemV2 = WalletRechargeItemV2(
        rechargeId = rechargeId,
        walletId = walletId,
        amount = amount,
        status = status,
        paymentGateway = paymentGateway,
        pgTransactionId = pgTransactionId,
        updatedWalletBalance = updatedWalletBalance,
        createdBy = createdBy,
        createdAt = createdAt,
        rechargeDate = rechargeDate
    )

    // ==================== WalletRechargeInitResponse ====================

    fun createRechargeInitResponse(
        rechargeId: String = "RCH_NEW_001",
        clRequestId: String = "CL_REQ_001",
        status: String = "initiated",
        paymentLinkUrl: String = "https://pay.example.com/link123",
        psOrderId: String = "PS_ORDER_001",
        psTxnId: String = "PS_TXN_001"
    ): WalletRechargeInitResponse = WalletRechargeInitResponse(
        rechargeId = rechargeId,
        clRequestId = clRequestId,
        status = status,
        paymentLinkUrl = paymentLinkUrl,
        psOrderId = psOrderId,
        psTxnId = psTxnId
    )

    // ==================== RechargeStatusResponse ====================

    fun createRechargeStatusResponse(
        rechargeId: String = "RCH001",
        walletId: String = "WALLET456",
        amount: String = "2000.00",
        status: String = "success",
        paymentGateway: String = "razorpay",
        createdAt: String = "2024-01-15T14:30:00",
        rechargeDate: String? = "2024-01-15",
        updatedWalletBalance: String = "7000.00",
        details: RechargePaymentDetails? = RechargePaymentDetails(pgTransactionId = "PG_TXN_001")
    ): RechargeStatusResponse = RechargeStatusResponse(
        rechargeId = rechargeId,
        walletId = walletId,
        amount = amount,
        status = status,
        paymentGateway = paymentGateway,
        createdAt = createdAt,
        rechargeDate = rechargeDate,
        updatedWalletBalance = updatedWalletBalance,
        details = details
    )

    fun createPendingRechargeStatus(
        rechargeId: String = "RCH_PENDING"
    ): RechargeStatusResponse = createRechargeStatusResponse(
        rechargeId = rechargeId,
        status = "pending"
    )
}

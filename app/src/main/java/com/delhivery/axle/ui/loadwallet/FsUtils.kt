package com.delhivery.axle.ui.loadwallet

import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.response.WalletTransactionItemV2
import com.delhivery.axle.utils.StringUtils


fun WalletTransactionItemV2.transactionHeading(): String {
    return when (txnReason.uppercase()) {
        "HYPERLOCAL" -> "Hyperlocal Payment"
        "WALLET_RECHARGE" -> "Wallet Recharge"
        "ORDER_PAYMENT" -> "Order Payment"
        "REFUND" -> "Refund"
        "FASTAG_RECHARGE" -> "FASTag Recharge"
        else -> txnReason.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

fun WalletTransactionItemV2.amountDouble(): Double = amount.toDoubleOrNull() ?: 0.0

fun UserWalletResponse.balanceFormatted(): String = "₹${StringUtils.formatAmount(currentBalance.toDoubleOrNull() ?: 0.0)}"


enum class TransactionType(val type: String) {
    Debit("debit"),
    Credit("credit")
}

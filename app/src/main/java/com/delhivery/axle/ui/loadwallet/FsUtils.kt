package com.delhivery.axle.ui.loadwallet

import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.response.WalletTransactionItem
import com.delhivery.axle.utils.StringUtils


fun WalletTransactionItem.transactionHeading(): String {
    return when (refType.uppercase()) {
        "HYPERLOCAL" -> "Hyperlocal Payment"
        "WALLET_RECHARGE" -> "Wallet Recharge"
        "ORDER_PAYMENT" -> "Order Payment"
        "REFUND" -> "Refund"
        "FASTAG_RECHARGE" -> "FASTag Recharge"
        else -> refType.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

fun WalletTransactionItem.amountDouble(): Double = amount.toDoubleOrNull() ?: 0.0

fun UserWalletResponse.balanceFormatted(): String = "₹${StringUtils.formatAmount(currentBalance)}"


enum class TransactionType(val type: String) {
    Debit("Debit"),
    Credit("Credit")
}
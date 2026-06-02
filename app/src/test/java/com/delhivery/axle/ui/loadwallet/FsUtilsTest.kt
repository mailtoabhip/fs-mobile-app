package com.delhivery.axle.ui.loadwallet

import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.response.WalletTransactionItemV2
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

/**
 * Unit tests for FsUtils.kt extension functions.
 *
 * Covers:
 * - WalletTransactionItemV2.transactionHeading() — all known reasons + fallback
 * - WalletTransactionItemV2.amountDouble() — valid/invalid/empty strings
 * - UserWalletResponse.balanceFormatted() — valid/invalid/zero balance
 */
class FsUtilsTest : FunSpec({

    // ==================== transactionHeading Tests ====================

    context("transactionHeading") {

        test("HYPERLOCAL maps to Hyperlocal Payment") {
            WalletTransactionItemV2(txnReason = "HYPERLOCAL").transactionHeading() shouldBe "Hyperlocal Payment"
        }

        test("WALLET_RECHARGE maps to Wallet Recharge") {
            WalletTransactionItemV2(txnReason = "WALLET_RECHARGE").transactionHeading() shouldBe "Wallet Recharge"
        }

        test("ORDER_PAYMENT maps to Order Payment") {
            WalletTransactionItemV2(txnReason = "ORDER_PAYMENT").transactionHeading() shouldBe "Order Payment"
        }

        test("REFUND maps to Refund") {
            WalletTransactionItemV2(txnReason = "REFUND").transactionHeading() shouldBe "Refund"
        }

        test("FASTAG_RECHARGE maps to FASTag Recharge") {
            WalletTransactionItemV2(txnReason = "FASTAG_RECHARGE").transactionHeading() shouldBe "FASTag Recharge"
        }

        test("unknown reason replaces underscores with spaces and title-cases first char") {
            WalletTransactionItemV2(txnReason = "CUSTOM_REASON").transactionHeading() shouldBe "Custom reason"
        }

        test("single-word unknown reason is lowercased then title-cased") {
            WalletTransactionItemV2(txnReason = "UNKNOWN").transactionHeading() shouldBe "Unknown"
        }

        test("lowercase known reason is uppercased before matching") {
            WalletTransactionItemV2(txnReason = "wallet_recharge").transactionHeading() shouldBe "Wallet Recharge"
        }

        test("mixed-case known reason is uppercased before matching") {
            WalletTransactionItemV2(txnReason = "Order_Payment").transactionHeading() shouldBe "Order Payment"
        }

        test("empty reason returns empty string") {
            WalletTransactionItemV2(txnReason = "").transactionHeading() shouldBe ""
        }
    }

    // ==================== amountDouble Tests ====================

    context("amountDouble") {

        test("valid decimal string returns correct double") {
            WalletTransactionItemV2(amount = "250.50").amountDouble() shouldBe 250.50
        }

        test("integer string returns correct double") {
            WalletTransactionItemV2(amount = "1000").amountDouble() shouldBe 1000.0
        }

        test("zero string returns 0.0") {
            WalletTransactionItemV2(amount = "0.00").amountDouble() shouldBe 0.0
        }

        test("invalid string returns 0.0") {
            WalletTransactionItemV2(amount = "N/A").amountDouble() shouldBe 0.0
        }

        test("empty string returns 0.0") {
            WalletTransactionItemV2(amount = "").amountDouble() shouldBe 0.0
        }

        test("whitespace-only string returns 0.0") {
            WalletTransactionItemV2(amount = " ").amountDouble() shouldBe 0.0
        }
    }

    // ==================== balanceFormatted Tests ====================

    context("balanceFormatted") {

        test("formatted balance starts with rupee symbol") {
            UserWalletResponse(currentBalance = "1000.00").balanceFormatted() shouldStartWith "₹"
        }

        test("zero balance formats as rupee zero") {
            UserWalletResponse(currentBalance = "0.00").balanceFormatted() shouldBe "₹0"
        }

        test("invalid balance string falls back to zero") {
            UserWalletResponse(currentBalance = "invalid").balanceFormatted() shouldBe "₹0"
        }

        test("empty balance string falls back to zero") {
            UserWalletResponse(currentBalance = "").balanceFormatted() shouldBe "₹0"
        }

        test("balance with no decimals formats correctly") {
            UserWalletResponse(currentBalance = "5000").balanceFormatted() shouldBe "₹5,000"
        }
    }
})

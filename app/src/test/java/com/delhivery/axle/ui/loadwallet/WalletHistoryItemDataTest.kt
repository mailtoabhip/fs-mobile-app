package com.delhivery.axle.ui.loadwallet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Unit tests for WalletHistoryItemData display logic.
 *
 * Covers:
 * - statusLabel() — normalizes all known and unknown status strings
 * - detailStatusLabel() — separate casing for detail screen
 * - amountFormatted() — sign prefix (+/-) based on transaction type
 * - isPending() — pending/processing detection
 * - isFailed() — failed/failure/rejected detection
 * - key() — uniqueness contract (txnNumber + dateTime)
 * - txnLabel() — TXN: prefix formatting
 */
class WalletHistoryItemDataTest : FunSpec({

    fun item(
        status: String = "success",
        type: String = "credit",
        amount: Double = 1000.0,
        txnNumber: String = "TXN001",
        dateTime: String = "2024-01-15T14:30:00"
    ) = WalletHistoryItemData(
        title = "Test",
        amount = amount,
        dateTime = dateTime,
        status = status,
        txnNumber = txnNumber,
        type = type
    )

    // ==================== statusLabel Tests ====================

    context("statusLabel") {

        test("success maps to SUCCESS") {
            item(status = "success").statusLabel() shouldBe "SUCCESS"
        }

        test("processed maps to SUCCESS") {
            item(status = "processed").statusLabel() shouldBe "SUCCESS"
        }

        test("pending maps to PENDING") {
            item(status = "pending").statusLabel() shouldBe "PENDING"
        }

        test("processing maps to PENDING") {
            item(status = "processing").statusLabel() shouldBe "PENDING"
        }

        test("failed maps to FAILED") {
            item(status = "failed").statusLabel() shouldBe "FAILED"
        }

        test("failure maps to FAILED") {
            item(status = "failure").statusLabel() shouldBe "FAILED"
        }

        test("rejected maps to FAILED") {
            item(status = "rejected").statusLabel() shouldBe "FAILED"
        }

        test("uppercase status is lowercased before matching") {
            item(status = "SUCCESS").statusLabel() shouldBe "SUCCESS"
        }

        test("unknown status is uppercased as-is") {
            item(status = "initiated").statusLabel() shouldBe "INITIATED"
        }
    }

    // ==================== detailStatusLabel Tests ====================

    context("detailStatusLabel") {

        test("success maps to SUCCESS") {
            item(status = "success").detailStatusLabel() shouldBe "SUCCESS"
        }

        test("processed maps to SUCCESS") {
            item(status = "processed").detailStatusLabel() shouldBe "SUCCESS"
        }

        test("pending maps to Pending (title case)") {
            item(status = "pending").detailStatusLabel() shouldBe "Pending"
        }

        test("processing maps to Pending (title case)") {
            item(status = "processing").detailStatusLabel() shouldBe "Pending"
        }

        test("failed maps to Failed Transaction") {
            item(status = "failed").detailStatusLabel() shouldBe "Failed Transaction"
        }

        test("failure maps to Failed Transaction") {
            item(status = "failure").detailStatusLabel() shouldBe "Failed Transaction"
        }

        test("rejected maps to Failed Transaction") {
            item(status = "rejected").detailStatusLabel() shouldBe "Failed Transaction"
        }

        test("unknown status is uppercased as-is") {
            item(status = "initiated").detailStatusLabel() shouldBe "INITIATED"
        }
    }

    // ==================== amountFormatted Tests ====================

    context("amountFormatted") {

        test("credit transaction prefixes amount with +") {
            item(type = "credit", amount = 1000.0).amountFormatted() shouldBe "+₹1,000"
        }

        test("debit transaction prefixes amount with -") {
            item(type = "debit", amount = 500.0).amountFormatted() shouldBe "-₹500"
        }

        test("type containing credit substring is treated as credit") {
            item(type = "wallet_credit", amount = 200.0).amountFormatted() shouldBe "+₹200"
        }

        test("zero amount formats correctly for credit") {
            item(type = "credit", amount = 0.0).amountFormatted() shouldBe "+₹0"
        }

        test("zero amount formats correctly for debit") {
            item(type = "debit", amount = 0.0).amountFormatted() shouldBe "-₹0"
        }
    }

    // ==================== isPending Tests ====================

    context("isPending") {

        test("pending status returns true") {
            item(status = "pending").isPending() shouldBe true
        }

        test("processing status returns true") {
            item(status = "processing").isPending() shouldBe true
        }

        test("success status returns false") {
            item(status = "success").isPending() shouldBe false
        }

        test("failed status returns false") {
            item(status = "failed").isPending() shouldBe false
        }

        test("uppercase PENDING is lowercased before check") {
            item(status = "PENDING").isPending() shouldBe true
        }
    }

    // ==================== isFailed Tests ====================

    context("isFailed") {

        test("failed status returns true") {
            item(status = "failed").isFailed() shouldBe true
        }

        test("failure status returns true") {
            item(status = "failure").isFailed() shouldBe true
        }

        test("rejected status returns true") {
            item(status = "rejected").isFailed() shouldBe true
        }

        test("success status returns false") {
            item(status = "success").isFailed() shouldBe false
        }

        test("pending status returns false") {
            item(status = "pending").isFailed() shouldBe false
        }

        test("uppercase FAILED is lowercased before check") {
            item(status = "FAILED").isFailed() shouldBe true
        }
    }

    // ==================== key Tests ====================

    context("key") {

        test("key is concatenation of txnNumber and dateTime") {
            item(txnNumber = "TXN123", dateTime = "2024-01-15T14:30:00").key() shouldBe "TXN1232024-01-15T14:30:00"
        }

        test("two items with same txnNumber but different dateTime have different keys") {
            val key1 = item(txnNumber = "TXN001", dateTime = "2024-01-15T10:00:00").key()
            val key2 = item(txnNumber = "TXN001", dateTime = "2024-01-16T10:00:00").key()
            (key1 == key2) shouldBe false
        }

        test("two items with same dateTime but different txnNumber have different keys") {
            val key1 = item(txnNumber = "TXN001", dateTime = "2024-01-15T10:00:00").key()
            val key2 = item(txnNumber = "TXN002", dateTime = "2024-01-15T10:00:00").key()
            (key1 == key2) shouldBe false
        }

        test("two identical items produce the same key") {
            val key1 = item(txnNumber = "TXN001", dateTime = "2024-01-15T10:00:00").key()
            val key2 = item(txnNumber = "TXN001", dateTime = "2024-01-15T10:00:00").key()
            key1 shouldBe key2
        }
    }

    // ==================== txnLabel Tests ====================

    context("txnLabel") {

        test("prefixes txnNumber with TXN: ") {
            item(txnNumber = "TXN123").txnLabel() shouldBe "TXN: TXN123"
        }

        test("empty txnNumber produces TXN: prefix only") {
            item(txnNumber = "").txnLabel() shouldBe "TXN: "
        }
    }
})

package com.delhivery.axle.api.response

import com.delhivery.axle.testdata.WalletTestDataFactory
import com.google.gson.Gson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

/**
 * Unit tests for Wallet API response models using Kotest.
 *
 * Covers:
 * - JSON deserialization for all wallet response models
 * - Default values
 * - Serializable contract
 * - Edge cases (empty lists, null fields)
 */
class WalletApiResponsesTest : FunSpec({

    val gson = Gson()

    // ==================== WalletRechargeInitResponse Tests ====================

    context("WalletRechargeInitResponse") {

        test("deserializes from JSON correctly") {
            val json = """
                {
                    "recharge_id": "RCH001",
                    "cl_request_id": "CL001",
                    "status": "initiated",
                    "payment_link_url": "https://pay.example.com/link",
                    "ps_order_id": "PS_ORD_001",
                    "ps_txn_id": "PS_TXN_001"
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletRechargeInitResponse::class.java)

            response.rechargeId shouldBe "RCH001"
            response.clRequestId shouldBe "CL001"
            response.status shouldBe "initiated"
            response.paymentLinkUrl shouldBe "https://pay.example.com/link"
            response.psOrderId shouldBe "PS_ORD_001"
            response.psTxnId shouldBe "PS_TXN_001"
        }

        test("has sensible defaults") {
            val response = WalletRechargeInitResponse()

            response.rechargeId shouldBe ""
            response.clRequestId shouldBe ""
            response.status shouldBe ""
            response.paymentLinkUrl shouldBe ""
            response.psOrderId shouldBe ""
            response.psTxnId shouldBe ""
        }

        test("is serializable") {
            val response = WalletTestDataFactory.createRechargeInitResponse()
            val serialized = gson.toJson(response)
            val deserialized = gson.fromJson(serialized, WalletRechargeInitResponse::class.java)

            deserialized.rechargeId shouldBe response.rechargeId
            deserialized.paymentLinkUrl shouldBe response.paymentLinkUrl
        }
    }

    // ==================== WalletTransactionHistoryResponse Tests ====================

    context("WalletTransactionHistoryResponse") {

        test("deserializes from JSON with transactions") {
            val json = """
                {
                    "wallet_id": "W001",
                    "total": 2,
                    "total_amount": "1500.00",
                    "has_next": true,
                    "next_cursor": "2024-01-10T10:00:00",
                    "transactions": [
                        {
                            "transaction_id": "TXN001",
                            "transaction_type": "credit",
                            "amount": "1000.00",
                            "status": "success",
                            "updated_wallet_balance": "6000.00",
                            "txn_reason": "WALLET_RECHARGE",
                            "txn_remarks": "UPI recharge",
                            "created_at": "2024-01-15T14:30:00"
                        }
                    ]
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletTransactionHistoryResponse::class.java)

            response.walletId shouldBe "W001"
            response.total shouldBe 2
            response.totalAmount shouldBe "1500.00"
            response.hasNext shouldBe true
            response.nextCursor shouldBe "2024-01-10T10:00:00"
            response.transactions shouldHaveSize 1
            response.transactions[0].transactionId shouldBe "TXN001"
            response.transactions[0].txnReason shouldBe "WALLET_RECHARGE"
        }

        test("handles empty transactions list") {
            val json = """
                {
                    "wallet_id": "W001",
                    "total": 0,
                    "total_amount": "0.00",
                    "has_next": false,
                    "transactions": []
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletTransactionHistoryResponse::class.java)

            response.transactions shouldHaveSize 0
            response.hasNext shouldBe false
            response.nextCursor.shouldBeNull()
        }

        test("has sensible defaults") {
            val response = WalletTransactionHistoryResponse()

            response.walletId shouldBe ""
            response.total shouldBe 0
            response.totalAmount shouldBe "0.00"
            response.hasNext shouldBe false
            response.nextCursor.shouldBeNull()
            response.transactions shouldHaveSize 0
        }
    }

    // ==================== WalletTransactionItemV2 Tests ====================

    context("WalletTransactionItemV2") {

        test("deserializes all fields correctly") {
            val json = """
                {
                    "transaction_id": "TXN_TEST",
                    "transaction_type": "debit",
                    "amount": "250.50",
                    "status": "pending",
                    "updated_wallet_balance": "4750.00",
                    "txn_reason": "ORDER_PAYMENT",
                    "txn_remarks": "Load payment",
                    "created_at": "2024-02-01T09:00:00"
                }
            """.trimIndent()

            val item = gson.fromJson(json, WalletTransactionItemV2::class.java)

            item.transactionId shouldBe "TXN_TEST"
            item.transactionType shouldBe "debit"
            item.amount shouldBe "250.50"
            item.status shouldBe "pending"
            item.updatedWalletBalance shouldBe "4750.00"
            item.txnReason shouldBe "ORDER_PAYMENT"
            item.txnRemarks shouldBe "Load payment"
            item.createdAt shouldBe "2024-02-01T09:00:00"
        }

        test("has sensible defaults") {
            val item = WalletTransactionItemV2()

            item.transactionId shouldBe ""
            item.transactionType shouldBe ""
            item.amount shouldBe "0.00"
            item.status shouldBe ""
            item.updatedWalletBalance shouldBe "0.00"
            item.txnReason shouldBe ""
            item.txnRemarks shouldBe ""
            item.createdAt shouldBe ""
        }
    }

    // ==================== WalletRechargeHistoryResponse Tests ====================

    context("WalletRechargeHistoryResponse") {

        test("deserializes from JSON with recharges") {
            val json = """
                {
                    "wallet_id": "W001",
                    "total": 1,
                    "total_amount": "2000.00",
                    "opening_balance": "3000.00",
                    "has_next": false,
                    "next_cursor": null,
                    "recharges": [
                        {
                            "recharge_id": "RCH001",
                            "wallet_id": "W001",
                            "amount": "2000.00",
                            "status": "success",
                            "payment_gateway": "razorpay",
                            "pg_transaction_id": "PG001",
                            "updated_wallet_balance": "5000.00",
                            "created_by": "USER1",
                            "created_at": "2024-01-15T14:30:00",
                            "recharge_date": "2024-01-15"
                        }
                    ]
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletRechargeHistoryResponse::class.java)

            response.walletId shouldBe "W001"
            response.total shouldBe 1
            response.totalAmount shouldBe "2000.00"
            response.openingBalance shouldBe "3000.00"
            response.hasNext shouldBe false
            response.recharges shouldHaveSize 1
            response.recharges[0].rechargeId shouldBe "RCH001"
            response.recharges[0].paymentGateway shouldBe "razorpay"
        }

        test("has sensible defaults") {
            val response = WalletRechargeHistoryResponse()

            response.walletId shouldBe ""
            response.total shouldBe 0
            response.totalAmount shouldBe "0.00"
            response.openingBalance shouldBe "0.00"
            response.hasNext shouldBe false
            response.nextCursor.shouldBeNull()
            response.recharges shouldHaveSize 0
        }
    }

    // ==================== WalletRechargeItemV2 Tests ====================

    context("WalletRechargeItemV2") {

        test("deserializes with nullable pg_transaction_id") {
            val json = """
                {
                    "recharge_id": "RCH002",
                    "wallet_id": "W001",
                    "amount": "500.00",
                    "status": "pending",
                    "payment_gateway": "cashfree",
                    "updated_wallet_balance": "0.00",
                    "created_by": "USER1",
                    "created_at": "2024-01-20T10:00:00"
                }
            """.trimIndent()

            val item = gson.fromJson(json, WalletRechargeItemV2::class.java)

            item.rechargeId shouldBe "RCH002"
            item.status shouldBe "pending"
            item.pgTransactionId.shouldBeNull()
            item.rechargeDate.shouldBeNull()
        }

        test("has sensible defaults") {
            val item = WalletRechargeItemV2()

            item.rechargeId shouldBe ""
            item.walletId shouldBe ""
            item.amount shouldBe "0.00"
            item.status shouldBe ""
            item.paymentGateway shouldBe ""
            item.pgTransactionId.shouldBeNull()
            item.updatedWalletBalance shouldBe "0.00"
            item.createdBy shouldBe ""
            item.createdAt shouldBe ""
            item.rechargeDate.shouldBeNull()
        }
    }

    // ==================== RechargeStatusResponse Tests ====================

    context("RechargeStatusResponse") {

        test("deserializes with nested details") {
            val json = """
                {
                    "recharge_id": "RCH001",
                    "wallet_id": "W001",
                    "amount": "2000.00",
                    "status": "success",
                    "payment_gateway": "razorpay",
                    "created_at": "2024-01-15T14:30:00",
                    "recharge_date": "2024-01-15",
                    "updated_wallet_balance": "7000.00",
                    "details": {
                        "pg_transaction_id": "PG_TXN_001"
                    }
                }
            """.trimIndent()

            val response = gson.fromJson(json, RechargeStatusResponse::class.java)

            response.rechargeId shouldBe "RCH001"
            response.status shouldBe "success"
            response.updatedWalletBalance shouldBe "7000.00"
            response.details.shouldNotBeNull()
            response.details!!.pgTransactionId shouldBe "PG_TXN_001"
        }

        test("handles null details") {
            val json = """
                {
                    "recharge_id": "RCH002",
                    "wallet_id": "W001",
                    "amount": "500.00",
                    "status": "pending",
                    "payment_gateway": "cashfree",
                    "created_at": "2024-01-20T10:00:00",
                    "updated_wallet_balance": "0.00"
                }
            """.trimIndent()

            val response = gson.fromJson(json, RechargeStatusResponse::class.java)

            response.details.shouldBeNull()
            response.rechargeDate.shouldBeNull()
        }

        test("has sensible defaults") {
            val response = RechargeStatusResponse()

            response.rechargeId shouldBe ""
            response.walletId shouldBe ""
            response.amount shouldBe "0.00"
            response.status shouldBe ""
            response.paymentGateway shouldBe ""
            response.createdAt shouldBe ""
            response.rechargeDate.shouldBeNull()
            response.updatedWalletBalance shouldBe "0.00"
            response.details.shouldBeNull()
        }
    }

    // ==================== RechargePaymentDetails Tests ====================

    context("RechargePaymentDetails") {

        test("deserializes pg_transaction_id") {
            val json = """{"pg_transaction_id": "PG123"}"""

            val details = gson.fromJson(json, RechargePaymentDetails::class.java)

            details.pgTransactionId shouldBe "PG123"
        }

        test("handles null pg_transaction_id") {
            val json = """{}"""

            val details = gson.fromJson(json, RechargePaymentDetails::class.java)

            details.pgTransactionId.shouldBeNull()
        }
    }

    // ==================== UserWalletResponse Tests ====================

    context("UserWalletResponse") {

        test("deserializes from JSON correctly") {
            val json = """
                {
                    "wallet_id": "W001",
                    "user_id": "U001",
                    "current_balance": "5000.50",
                    "min_th": "500.00",
                    "max_th": "100000.00",
                    "locked_amount": "200.00",
                    "is_active": true,
                    "email": "test@example.com",
                    "phone": "9876543210",
                    "created_at": "2024-01-01T10:00:00",
                    "updated_at": "2024-01-15T14:30:00"
                }
            """.trimIndent()

            val response = gson.fromJson(json, UserWalletResponse::class.java)

            response.walletId shouldBe "W001"
            response.userId shouldBe "U001"
            response.currentBalance shouldBe "5000.50"
            response.minThreshold shouldBe "500.00"
            response.maxThreshold shouldBe "100000.00"
            response.lockedAmount shouldBe "200.00"
            response.isActive shouldBe true
            response.email shouldBe "test@example.com"
            response.phone shouldBe "9876543210"
        }

        test("has sensible defaults") {
            val response = UserWalletResponse()

            response.walletId shouldBe ""
            response.userId shouldBe ""
            response.currentBalance shouldBe "0.00"
            response.minThreshold shouldBe "0.00"
            response.maxThreshold shouldBe "0.00"
            response.lockedAmount shouldBe "0.00"
            response.isActive shouldBe false
            response.email shouldBe ""
            response.phone shouldBe ""
        }
    }

    // ==================== WalletTransactionHistoryResponse — missing fields ====================

    context("WalletTransactionHistoryResponse - txnRemarks field") {

        test("deserializes top-level txn_remarks") {
            val json = """
                {
                    "wallet_id": "W001",
                    "total": 0,
                    "total_amount": "0.00",
                    "has_next": false,
                    "transactions": [],
                    "txn_remarks": "Monthly summary"
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletTransactionHistoryResponse::class.java)

            response.txnRemarks shouldBe "Monthly summary"
        }

        test("txn_remarks defaults to empty string") {
            val response = WalletTransactionHistoryResponse()

            response.txnRemarks shouldBe ""
        }
    }

    // ==================== WalletRechargeHistoryResponse — missing fields ====================

    context("WalletRechargeHistoryResponse - bankReferenceNo and paymentMethod fields") {

        test("deserializes bank_reference_no and payment_method") {
            val json = """
                {
                    "wallet_id": "W001",
                    "total": 0,
                    "total_amount": "0.00",
                    "has_next": false,
                    "recharges": [],
                    "bank_reference_no": "NEFT123456",
                    "payment_method": "UPI"
                }
            """.trimIndent()

            val response = gson.fromJson(json, WalletRechargeHistoryResponse::class.java)

            response.bankReferenceNo shouldBe "NEFT123456"
            response.paymentMethod shouldBe "UPI"
        }

        test("bankReferenceNo and paymentMethod default to empty string") {
            val response = WalletRechargeHistoryResponse()

            response.bankReferenceNo shouldBe ""
            response.paymentMethod shouldBe ""
        }
    }

    // ==================== WalletData Model Method Tests ====================

    context("WalletData") {

        fun walletData(
            balance: Double = 1000.0,
            accNumber: String = "1234567890"
        ) = WalletData(
            active = true,
            autoWithdraw = false,
            balance = balance,
            walletId = "W001",
            ifsc = "HDFC0001234",
            accName = "Test Supplier",
            accNumber = accNumber,
            accType = "savings",
            optinDate = "2024-01-01T10:00:00"
        )

        context("balance()") {

            test("formatted balance starts with rupee symbol and space") {
                walletData(balance = 1000.0).balance() shouldStartWith "₹ "
            }

            test("zero balance formats as zero") {
                walletData(balance = 0.0).balance() shouldBe "₹ 0"
            }
        }

        context("accNumber()") {

            test("masks all but last 4 digits with asterisks") {
                walletData(accNumber = "1234567890").accNumber() shouldBe "******7890"
            }

            test("5-digit account number masks first digit") {
                walletData(accNumber = "12345").accNumber() shouldBe "*2345"
            }

            test("exactly 4-digit account number has no masking") {
                walletData(accNumber = "1234").accNumber() shouldBe "1234"
            }
        }
    }
})

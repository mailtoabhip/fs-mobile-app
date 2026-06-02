package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class PaymentBreakupResponse(
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("breakup")
    val breakup: List<BreakupLineItem>,
    @SerializedName("grand_total")
    val grandTotal: String,
    @SerializedName("items")
    val items: List<PaymentBreakupOrderItem>,
    @SerializedName("wallet")
    val wallet: WalletSnapshot,
    @SerializedName("can_proceed")
    val canProceed: Boolean,
    @SerializedName("message")
    val message: String?
)

data class BreakupLineItem(
    @SerializedName("label")
    val label: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("amount")
    val amount: String
)

data class PaymentBreakupOrderItem(
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: String,
    @SerializedName("line_total")
    val lineTotal: String
)

data class WalletSnapshot(
    @SerializedName("wallet_id")
    val walletId: String,
    @SerializedName("current_balance")
    val currentBalance: String,
    @SerializedName("locked_amount")
    val lockedAmount: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("sufficient")
    val sufficient: Boolean,
    @SerializedName("shortfall")
    val shortfall: String?
)

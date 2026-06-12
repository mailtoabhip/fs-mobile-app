package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class CreateOrderResponse(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("vendor_id")
    val vendorId: String,
    @SerializedName("partner_code")
    val partnerCode: String,
    @SerializedName("customer_name")
    val customerName: String,
    @SerializedName("customer_mobile")
    val customerMobile: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("paid_amount")
    val paidAmount: Double?,
    @SerializedName("payment_txn_id")
    val paymentTxnId: String?,
    @SerializedName("idempotency_key")
    val idempotencyKey: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("items")
    val items: List<CreateOrderItemResponse>,
    @SerializedName("item_count")
    val itemCount: Int
)

data class CreateOrderItemResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("vrn")
    val vrn: String,
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("unit_price")
    val unitPrice: Double,
    @SerializedName("created_at")
    val createdAt: String
)

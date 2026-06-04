package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagOrdersResponse(
    @SerializedName("count")
    val count: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("has_more")
    val hasMore: Boolean,
    @SerializedName("orders")
    val orders: List<FastagOrder>
)

data class FastagOrder(
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
    @SerializedName("payment_txn_id")
    val paymentTxnId: String,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("paid_amount")
    val paidAmount: Double?,
    @SerializedName("items")
    val items: List<FastagOrderItem>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class FastagOrderItem(
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("vehicle_types")
    val vehicleTypes: List<String>,
    @SerializedName("color_code")
    val colorCode: String,
    @SerializedName("vrn")
    val vrn: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("unit_price")
    val unitPrice: Double
)

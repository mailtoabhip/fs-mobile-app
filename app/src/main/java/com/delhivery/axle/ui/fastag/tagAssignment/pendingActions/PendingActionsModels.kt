package com.delhivery.axle.ui.fastag.tagAssignment.pendingActions

import com.google.gson.annotations.SerializedName

/**
 * Represents a pending order with its vehicles requiring action.
 */
data class PendingOrder(
    val orderId: String,
    val date: String,
    val pendingCount: Int,
    val vehicles: List<PendingVehicle>,
    var isExpanded: Boolean = false
)

/**
 * Represents a vehicle within a pending order that requires action.
 *
 * @param colorCode Color code string from API: "ORANGE", "YELLOW", "GREEN", "PINK", "BLUE", "ORANGE"
 */
data class PendingVehicle(
    val vehicleClass: String,
    val referenceId: String?,
    val vehicleNumber: String?,
    val actionType: PendingActionType,
    val actionLabel: String,
    val colorCode: String = "GREEN",
    val barcodeId: String? = null,
    val salesCode: String? = null,
    val orderId: String? = null,
    val itemId: String? = null,
    val journeyId: String? = null,
    val items: ArrayList<com.delhivery.axle.api.request.PaymentBreakupItem>? = null
)

/**
 * Types of pending actions for FASTag vehicles.
 */
enum class PendingActionType(val displayName: String) {
    ADD_VEHICLE("Add Vehicle"),
    ORDER_CREATED("Order Created"),
    KYC_DONE("KYC"),
    FULL_PAYMENT_PARTIAL_PAYMENT("Payment Pending"),
    TAG_ASSIGNMENT("Vehicle Assignment"),
    KYV("KYV"),
    HOTO_DONE("Handover")
}


// ---------------------------------------------------------------------------
// API Response Models — GET /fastag/v1/pending-actions
// ---------------------------------------------------------------------------

data class PendingActionsResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("pending_actions") val pendingActions: List<PendingActionOrder>
)

/**
 * Represents a grouped order from the API response.
 */
data class PendingActionOrder(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("sales_code") val salesCode: String?,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("items") val items: List<PendingActionItem>,
    @SerializedName("vehicle_class_summary") val vehicleClassSummary: List<VehicleClassSummary>?
)

data class PendingActionItem(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("vrn") val vrn: String,
    @SerializedName("vehicle_class") val vehicleClass: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("color_code") val colorCode: String,
    @SerializedName("journey_id") val journeyId: String?,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("barcode_id") val barcodeId: String?,
    @SerializedName("tag_id") val tagId: String?,
    @SerializedName("current_milestone") val currentMilestone: String,
    @SerializedName("next_action") val nextAction: NextAction
)

data class VehicleClassSummary(
    @SerializedName("vehicle_class") val vehicleClass: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("color_code") val colorCode: String,
    @SerializedName("count") val count: Int
)

data class NextAction(
    @SerializedName("key") val key: String,
    @SerializedName("label") val label: String,
    @SerializedName("screen") val screen: String
)

package com.delhivery.axle.ui.fastag.pending

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
 * @param colorCode Color code string from API: "RED", "YELLOW", "GREEN", "PINK", "BLUE"
 */
data class PendingVehicle(
    val vehicleClass: String,
    val referenceId: String?,
    val vehicleNumber: String?,
    val actionType: PendingActionType,
    val colorCode: String = "GREEN"
)

/**
 * Types of pending actions for FASTag vehicles.
 */
enum class PendingActionType(val displayName: String) {
    ASSIGNMENT("Assignment"),
    ACTIVATION("Activation"),
    HANDOVER("Handover"),
    KYC("KYC")
}

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
 */
data class PendingVehicle(
    val vehicleClass: String,
    val referenceId: String?,
    val vehicleNumber: String?,
    val actionType: PendingActionType
)

/**
 * Types of pending actions for FASTag vehicles.
 */
enum class PendingActionType(val displayName: String) {
    ASSIGNMENT("Assignment"),
    ACTIVATION("Activation"),
    HANDOVER("Handover"),
    KYV("KYV")
}

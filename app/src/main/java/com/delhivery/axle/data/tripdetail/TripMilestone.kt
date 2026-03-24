package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.R

/**
 * Represents a milestone in the trip timeline.
 * This is a loosely coupled model that allows easy modification of milestones.
 */
data class TripMilestone(
    val id: String,
    val displayName: String,
    val status: MilestoneStatus = MilestoneStatus.PENDING,
    val timestamp: String? = null,
    val subtitle: String? = null,
    val subtitleColorRes: Int? = null
) {
    fun getIconRes(): Int = when (status) {
        MilestoneStatus.COMPLETED -> R.drawable.ic_check_circle_green
        MilestoneStatus.PENDING -> R.drawable.ic_rounded_circle_black
        MilestoneStatus.FAILED -> R.drawable.fs_payment_failed
    }
}

/**
 * Status of a milestone
 */
enum class MilestoneStatus {
    COMPLETED,
    PENDING,
    FAILED
}

/**
 * Milestone IDs for easy reference and modification
 * 
 * GST Vendor (7-step flow):
 * 1. ARRIVED_AT_PICKUP - from status_update_info(truck_arrived)
 * 2. LOADED - from status_update_info(truck_loaded)
 * 3. TRIP_COMPLETED - from status_update_info(trip_completed)
 * 4. TICKET_CLOSED - from invoice_status_info(ticket_status = "closed")
 * 5. ACCEPT_INVOICE - from invoice_status_info(invoice_status = "invoiced")
 * 6. INVOICE_ACCEPTED - from invoice_status_info(invoice_status in [under_finance_review, paid])
 * 7. PAYMENT_RELEASED - from invoice_status_info(invoice_status = "paid")
 * 
 * Non-GST Vendor (5-step flow):
 * 1. ARRIVED_AT_PICKUP - from status_update_info(truck_arrived)
 * 2. LOADED - from status_update_info(truck_loaded)
 * 3. TRIP_COMPLETED - from status_update_info(trip_completed)
 * 4. TICKET_CLOSED - from invoice_status_info(ticket_status = "closed" && invoice_status exists)
 * 5. PAYMENT_RELEASED - from invoice_status_info(invoice_status = "paid")
 */
object MilestoneIds {
    // Common milestones (from status_update_info)
    const val ARRIVED_AT_PICKUP = "arrived_at_pickup"
    const val LOADED = "loaded"
    const val TRIP_COMPLETED = "trip_completed"
    const val REACHED_DESTINATION = "reached_destination"
    const val UNLOADED = "unloaded"
    const val HPOD_SUBMITTED = "hpod_submitted"
    const val SETTLED = "settled"

    // Invoice milestones (from invoice_status_info)
    const val TICKET_CLOSED = "ticket_closed"
    const val ACCEPT_INVOICE = "accept_invoice"
    const val INVOICE_ACCEPTED = "invoice_accepted"
    const val PAYMENT_RELEASED = "payment_released"
}

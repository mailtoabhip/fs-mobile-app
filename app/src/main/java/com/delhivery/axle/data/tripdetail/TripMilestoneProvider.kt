package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.InvoiceStatusInfo
import com.delhivery.axle.data.home.trips.TicketStatus
import com.delhivery.axle.data.home.trips.TripInvoiceStatus
import com.google.common.base.Ticker

/**
 * Provider for trip milestones based on vendor type and trip status.
 * 
 * GST Vendor (7-step flow):
 * 1. Arrived at Pickup - from status_update_info(truck_arrived)
 * 2. Loaded - from status_update_info(truck_loaded)
 * 3. Mark Out / Trip Completed - from status_update_info(trip_completed)
 * 4. Ticket Closed - active when ticket_status = "closed"
 * 5. Accept Invoice / Billing Under Review - active when invoice_status = "invoice_under_review"
 * 6. Invoice Accepted - active when invoice_status in (accepted, invoiced, paid)
 * 7. Payment Released - active when invoice_status = "paid"
 * 
 * Non-GST Vendor (5-step flow):
 * 1. Arrived at Pickup - from status_update_info(truck_arrived)
 * 2. Loaded - from status_update_info(truck_loaded)
 * 3. Mark Out / Trip Completed - from status_update_info(trip_completed)
 * 4. Ticket Closed - active when ticket_status = "closed" && invoice_status exists
 * 5. Payment Released - active when invoice_status = "paid"
 */
object TripMilestoneProvider {
    fun getAdhocIntracityMilestones(
        tripDetails: HomeTripsItemData,
        isGstVerified :Boolean,
        settledTime:String?
    ): List<TripMilestone> {
        val invoiceStatusInfo = tripDetails.invoiceStatusInfo
        return if(invoiceStatusInfo==null){
                getLegacyVendorMilestonesFromInfo(tripDetails, settledTime)
            }else if (invoiceStatusInfo.isGstVendor || isGstVerified) {
                getGstVendorMilestonesFromInfo(tripDetails)
            } else {
                getNonGstVendorMilestonesFromInfo(tripDetails)
            }
    }

    /**
     * GST Vendor 7-step flow using invoice_status_info
     */
    private fun getGstVendorMilestonesFromInfo(
        tripDetails: HomeTripsItemData
    ): List<TripMilestone> {
        val milestones = mutableListOf<TripMilestone>()
        val info = tripDetails.invoiceStatusInfo
        val invoiceStatus = TripInvoiceStatus.fromValue(info?.invoiceStatus)

        milestones.add(createArrivedMilestone(tripDetails))
        milestones.add(createLoadedMilestone(tripDetails))
        milestones.add(createTripCompletedMilestone(tripDetails))
        milestones.add(createTicketClosedMilestone(tripDetails, true))

        val isInvoiceAccepted = invoiceStatus == TripInvoiceStatus.ACCEPTED ||
                invoiceStatus == TripInvoiceStatus.PAID ||
                invoiceStatus == TripInvoiceStatus.PAYMENT_FAILED
        // 5. Accept Invoice / Billing Under Review - active when invoice_status exists
        val isInvoiceReviewed = invoiceStatus == TripInvoiceStatus.UNDER_FINANCE_REVIEW
        val acceptInvoiceStatus = when {
            isInvoiceReviewed || isInvoiceAccepted -> MilestoneStatus.COMPLETED
            else -> MilestoneStatus.PENDING
        }
        milestones.add(
            TripMilestone(
                id = MilestoneIds.ACCEPT_INVOICE,
                displayName = "Accept Invoice / Billing Under Review",
                status = acceptInvoiceStatus
            )
        )
        
        // 6. Invoice Accepted - active when invoice_status in (under_finance_review, paid)
        val invoiceAcceptedStatus = when {
            isInvoiceAccepted -> MilestoneStatus.COMPLETED
            else -> MilestoneStatus.PENDING
        }
        milestones.add(
            TripMilestone(
                id = MilestoneIds.INVOICE_ACCEPTED,
                displayName = "Invoice Accepted",
                status = invoiceAcceptedStatus
            )
        )
        milestones.add(createPaymentStatusMilestone(tripDetails ))
        return milestones
    }

    /**
     * Non-GST Vendor 5-step flow using invoice_status_info
     */
    private fun getNonGstVendorMilestonesFromInfo(
        tripDetails: HomeTripsItemData,
    ): List<TripMilestone> {
        val milestones = mutableListOf<TripMilestone>()
        milestones.add(createArrivedMilestone(tripDetails))
        milestones.add(createLoadedMilestone(tripDetails))
        milestones.add(createTripCompletedMilestone(tripDetails))
        milestones.add(createTicketClosedMilestone(tripDetails, false))
        milestones.add(createPaymentStatusMilestone(tripDetails ))
        return milestones
    }

    private fun getLegacyVendorMilestonesFromInfo(
        tripDetails: HomeTripsItemData,
        settledTime:String?
    ): List<TripMilestone> {
        val milestones = mutableListOf<TripMilestone>()
        milestones.add(createArrivedMilestone(tripDetails))
        milestones.add(createLoadedMilestone(tripDetails))
        milestones.add(createReachedDestinationMilestone(tripDetails))
        milestones.add(createUnloadedMilestone(tripDetails))
        milestones.add(createHpodMilestone(tripDetails))
        milestones.add(createSettledMilestone(tripDetails,settledTime))
        return milestones
    }

    /**
     * Build payment subtitle from payment info
     */
    private fun buildPaymentSubtitle(paymentInfo: com.delhivery.axle.data.home.trips.InvoicePaymentInfo?): String? {
        if (paymentInfo == null) return null
        return try {
            val parts = mutableListOf<String>()
            paymentInfo.amount?.let {
                parts.add("₹${com.delhivery.axle.utils.StringUtils.formatAmount(it)}")
            }
            paymentInfo.utr?.let {
                parts.add("UTR: $it")
            }
            if (parts.isNotEmpty()) parts.joinToString(" | ") else null
        } catch (e: Exception) {
            null
        }
    }

    private fun formatPaymentTimestamp(info: InvoiceStatusInfo): String? {
        return info.paymentInfo?.paymentTimestamp?.let { formatDateString(it) }
    }

    private fun formatDateString(timestamp: String): String? {
        return try {
            val date = com.delhivery.axle.utils.DateUtils.parseDate(
                timestamp,
                com.delhivery.axle.utils.DatePatterns.OrionDateFormat
            )
            val sdf = java.text.SimpleDateFormat("dd-MMM-yyyy hh:mma", java.util.Locale.ENGLISH)
            sdf.format(date)
        } catch (e: Exception) {
            null
        }
    }

    private fun createArrivedMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        val arrivedInfo = tripDetails.updateInfo?.truckArrivedInfo
        val isArrived = arrivedInfo != null
        return TripMilestone(
            id = MilestoneIds.ARRIVED_AT_PICKUP,
            displayName = "Arrived at Pickup",
            status = if (isArrived) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //arrivedInfo?.time?.let { formatDateString(it) }
        )
    }

    private fun createLoadedMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        val loadedInfo = tripDetails.updateInfo?.loadedInfo
        val isLoaded = loadedInfo != null
        return TripMilestone(
            id = MilestoneIds.LOADED,
            displayName = "Loaded",
            status = if (isLoaded) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //  loadedInfo?.time?.let { formatDateString(it) }
        )
    }

    /*Legacy Support Starts*/
    private fun createReachedDestinationMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        return TripMilestone(
            id = MilestoneIds.REACHED_DESTINATION,
            displayName = "Reached Destination",
            status = if (tripDetails.updateInfo?.truckReachedInfo!=null) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //tripCompletedInfo?.time?.let { formatDateString(it) }
        )
    }
    private fun createUnloadedMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        return TripMilestone(
            id = MilestoneIds.UNLOADED,
            displayName = "Unloaded",
            status = if (tripDetails.updateInfo?.truckUnloadedInfo!=null) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //tripCompletedInfo?.time?.let { formatDateString(it) }
        )
    }
    private fun createHpodMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        return TripMilestone(
            id = MilestoneIds.UNLOADED,
            displayName = "hPOD Submitted",
            status = if (tripDetails.updateInfo?.tripCompletedInfo!=null) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //tripCompletedInfo?.time?.let { formatDateString(it) }
        )
    }
    private fun createSettledMilestone(tripDetails: HomeTripsItemData,settledTime:String?): TripMilestone {
        return TripMilestone(
            id = MilestoneIds.UNLOADED,
            displayName = "Settled",
            status = if (tripDetails.isSettled) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = settledTime?.let { formatDateString(it) }
        )
    }
    /*Legacy Support Ends*/
    private fun createTripCompletedMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        val tripCompletedInfo = tripDetails.updateInfo?.tripCompletedInfo
        val isTripCompleted = tripCompletedInfo != null
        return TripMilestone(
            id = MilestoneIds.TRIP_COMPLETED,
            displayName = "Mark Out / Trip Completed",
            status = if (isTripCompleted) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //tripCompletedInfo?.time?.let { formatDateString(it) }
        )
    }
    private fun createTicketClosedMilestone(tripDetails: HomeTripsItemData, isGstUser :Boolean): TripMilestone {
        val ticketStatus = TicketStatus.fromValue(tripDetails.invoiceStatusInfo?.ticketStatus)
        val invoiceStatus = TripInvoiceStatus.fromValue(tripDetails.invoiceStatusInfo?.invoiceStatus)

        val isTicketClosed = (ticketStatus == TicketStatus.CLOSED || ticketStatus == TicketStatus.PAID) && (isGstUser || invoiceStatus != null)
        val ticketClosedStatus = when {
            isTicketClosed -> MilestoneStatus.COMPLETED
            else -> MilestoneStatus.PENDING
        }
        return TripMilestone(
            id = MilestoneIds.TICKET_CLOSED,
            displayName = "Ticket Closed",
            status = ticketClosedStatus,
            timestamp = null
        )

    }
    private fun createPaymentStatusMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        val invoiceStatus = TripInvoiceStatus.fromValue(tripDetails.invoiceStatusInfo?.invoiceStatus)
        val info = tripDetails.invoiceStatusInfo
        val paymentReleasedStatus = when (invoiceStatus) {
            TripInvoiceStatus.PAID -> MilestoneStatus.COMPLETED
            TripInvoiceStatus.PAYMENT_FAILED -> MilestoneStatus.FAILED
            else ->MilestoneStatus.PENDING
        }
        val paymentDisplayName = when (invoiceStatus) {
            TripInvoiceStatus.PAYMENT_FAILED -> "Payment Failed"
            else -> "Payment Released"
        }
        return TripMilestone(
                id = MilestoneIds.PAYMENT_RELEASED,
                displayName = paymentDisplayName,
                status = paymentReleasedStatus,
                timestamp = if (invoiceStatus == TripInvoiceStatus.PAID && info!= null) formatPaymentTimestamp(info) else null,
                subtitle = info?.let {
                    if (invoiceStatus == TripInvoiceStatus.PAYMENT_FAILED) info.failureMessage
                    else buildPaymentSubtitle(info.paymentInfo)
                }
            )
    }
}

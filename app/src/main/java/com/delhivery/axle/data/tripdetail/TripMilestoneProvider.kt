package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.InvoiceStatusInfo
import com.delhivery.axle.data.home.trips.TicketStatus
import com.delhivery.axle.data.home.trips.TripInvoiceStatus
object TripMilestoneProvider {

    fun getIntracityMilestones(
        tripDetails: HomeTripsItemData
    ): List<TripMilestone>{
        return getLegacyVendorMilestonesFromInfo(tripDetails)
    }

    fun getOpsArrangedIntracityMilestones(
        tripDetails: HomeTripsItemData,
        isGstVerified:Boolean
    ): List<TripMilestone> {
        val invoiceStatusInfo = tripDetails.invoiceStatusInfo
        val isGstVendor = invoiceStatusInfo?.isGstVendor ?: isGstVerified
        return if (isGstVendor) {
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
    ): List<TripMilestone> = buildList {
        val info = tripDetails.invoiceStatusInfo
        val invoiceStatus = TripInvoiceStatus.fromValue(info?.invoiceStatus)

        add(createArrivedMilestone(tripDetails))
        add(createLoadedMilestone(tripDetails))
        add(createTripCompletedMilestone(tripDetails))
        add(createTicketClosedMilestone(tripDetails, true))

        val isInvoiceAccepted = invoiceStatus == TripInvoiceStatus.ACCEPTED ||
                invoiceStatus == TripInvoiceStatus.INVOICED ||
                invoiceStatus == TripInvoiceStatus.PAID ||
                invoiceStatus == TripInvoiceStatus.PAYMENT_FAILED

        val acceptInvoiceStatus = when {
            invoiceStatus == TripInvoiceStatus.INVOICE_UNDER_REVIEW || isInvoiceAccepted -> MilestoneStatus.COMPLETED
            else -> MilestoneStatus.PENDING
        }
        add(
            TripMilestone(
                id = MilestoneIds.ACCEPT_INVOICE,
                displayName = "Review Invoice",
                status = acceptInvoiceStatus
            )
        )
        
        val invoiceAcceptedStatus = when {
            isInvoiceAccepted -> MilestoneStatus.COMPLETED
            else -> MilestoneStatus.PENDING
        }
        add(
            TripMilestone(
                id = MilestoneIds.INVOICE_ACCEPTED,
                displayName = "Invoice Accepted",
                status = invoiceAcceptedStatus
            )
        )
        add(createPaymentStatusMilestone(tripDetails))
    }

    /**
     * Non-GST Vendor 5-step flow using invoice_status_info
     */
    private fun getNonGstVendorMilestonesFromInfo(
        tripDetails: HomeTripsItemData
    ): List<TripMilestone> = buildList {
        add(createArrivedMilestone(tripDetails))
        add(createLoadedMilestone(tripDetails))
        add(createTripCompletedMilestone(tripDetails))
        add(createTicketClosedMilestone(tripDetails, false))
        add(createPaymentStatusMilestone(tripDetails))
    }

    /**
     * Non-Ops Arranged trips 6-step flow
     */
    private fun getLegacyVendorMilestonesFromInfo(
        tripDetails: HomeTripsItemData
    ): List<TripMilestone> = buildList {
        add(createArrivedMilestone(tripDetails))
        add(createLoadedMilestone(tripDetails))
        add(createReachedDestinationMilestone(tripDetails))
        add(createUnloadedMilestone(tripDetails))
        add(createHpodMilestone(tripDetails))
        add(createSettledMilestone(tripDetails))
    }

    private fun formatPaymentTimestamp(info: InvoiceStatusInfo?, isSuccess: Boolean): String? {
        if (info == null) return null
        val paymentInfo = info.paymentInfo ?: return null
        val timestamp = paymentInfo.paymentTimestamp?.let { formatDateString(it) } ?: return null
        
        return buildString {
            if (isSuccess) {
                append("Payment made at ")
                append(timestamp)
                paymentInfo.utr?.let { utr ->
                    append("\nReference number : ")
                    append(utr)
                }
            } else {
                append("Failed at ")
                append(timestamp)
            }
        }
    }

    /*Returns current date if invalid timestamp was passed as param*/
     fun formatDateString(timestamp: String?): String? {
        if (timestamp.isNullOrBlank()) return null
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
            id = MilestoneIds.HPOD_SUBMITTED,
            displayName = "hPOD Submitted",
            status = if (tripDetails.updateInfo?.tripCompletedInfo!=null) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null //tripCompletedInfo?.time?.let { formatDateString(it) }
        )
    }
    private fun createSettledMilestone(tripDetails: HomeTripsItemData): TripMilestone {
        return TripMilestone(
            id = MilestoneIds.SETTLED,
            displayName = "Settled",
            status = if (tripDetails.isSettled) MilestoneStatus.COMPLETED else MilestoneStatus.PENDING,
            timestamp = null
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
        val isFailure = invoiceStatus == TripInvoiceStatus.PAYMENT_FAILED
        return TripMilestone(
                id = MilestoneIds.PAYMENT_RELEASED,
                displayName = paymentDisplayName,
                status = paymentReleasedStatus,
                timestamp = if ((invoiceStatus == TripInvoiceStatus.PAID || invoiceStatus == TripInvoiceStatus.PAYMENT_FAILED) && info!= null) formatPaymentTimestamp(info, invoiceStatus == TripInvoiceStatus.PAID) else null,
                subtitle = if (isFailure && info?.failureMessage != null) {
                    "Failure Remarks: ${info.failureMessage}"
                } else {
                    null
                },
                subtitleColorRes = if (isFailure) R.color.failure_red else R.color.icon_color
            )
    }
}

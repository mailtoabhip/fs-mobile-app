package com.delhivery.axle.testdata

import com.delhivery.axle.data.home.trips.ByUser
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.InvoicePaymentInfo
import com.delhivery.axle.data.home.trips.InvoiceStatusInfo
import com.delhivery.axle.data.home.trips.StatusUpdateInfo
import com.delhivery.axle.data.home.trips.TripBidDetails
import com.delhivery.axle.data.home.trips.TripDriverDetails
import com.delhivery.axle.data.home.trips.TripVehicleDetails

/**
 * Factory for creating test data objects for Trip-related tests.
 * Provides sensible defaults that can be overridden as needed.
 */
object TripTestDataFactory {

    // ==================== HomeTripsItemData ====================

    /**
     * Creates a base trip details object with sensible defaults.
     * Override only the fields you need for your specific test case.
     */
    fun createBaseTripDetails(
        transactionId: String = "TXN123",
        lr: String = "LR123",
        tripStatus: String = "truck_confirmed",
        origin: String = "Delhi",
        originState: String = "Delhi",
        destination: String = "Mumbai",
        destinationState: String = "Maharashtra",
        clientId: String = "CLIENT1",
        vehicleNo: String = "MH12AB1234",
        driverName: String = "Driver Name",
        driverPhone: String = "9876543210",
        requiredOn: String = "2024-01-02",
        actionTime: String = "2024-01-01T10:00:00",
        isSettled: Boolean = false,
        tds: Int = 0,
        updatedTds: Double = 0.0,
        invoiceStatusInfo: InvoiceStatusInfo? = null,
        updateInfo: StatusUpdateInfo? = null
    ): HomeTripsItemData = HomeTripsItemData(
        lr = lr,
        arrivalTime = null,
        actionTime = actionTime,
        clientId = clientId,
        destination = destination,
        destinationState = destinationState,
        origin = origin,
        originState = originState,
        transactionId = transactionId,
        tripStatus = tripStatus,
        vehicleDetails = TripVehicleDetails(vehicleNo),
        driverDetails = TripDriverDetails(driverName, driverPhone),
        bidDetails = null,
        loadingLocation = null,
        loadingLocationContactNo = null,
        reachedTime = null,
        unloadingTime = null,
        requiredOn = requiredOn,
        requiredOnTime = null,
        unloadingLocation = null,
        unloadingLocationContactNo = null,
        truckSpecification = null,
        podDispatchAwbNumber = null,
        podDispatchDocketImage = null,
        podDispatchDate = null,
        updateInfo = updateInfo,
        speed = null,
        tatMinutes = null,
        originDistrict = null,
        destinationDistrict = null,
        entity = null,
        subRequestType = null,
        tds = tds,
        updatedTds = updatedTds,
        isSettled = isSettled,
        invoiceStatusInfo = invoiceStatusInfo
    )

    // ==================== InvoiceStatusInfo ====================

    /**
     * Creates GST vendor invoice status info.
     */
    fun createGstVendorInvoiceInfo(
        ticketStatus: String? = null,
        invoiceStatus: String? = null,
        showReviewInvoiceCta: Boolean = false,
        showDownloadInvoice: Boolean = false,
        failureMessage: String? = null,
        paymentInfo: InvoicePaymentInfo? = null
    ): InvoiceStatusInfo = InvoiceStatusInfo(
        isGstVendor = true,
        ticketStatus = ticketStatus,
        invoiceStatus = invoiceStatus,
        showReviewInvoiceCta = showReviewInvoiceCta,
        showDownloadInvoice = showDownloadInvoice,
        paymentInfo = paymentInfo,
        failureMessage = failureMessage
    )

    /**
     * Creates non-GST vendor invoice status info.
     */
    fun createNonGstVendorInvoiceInfo(
        ticketStatus: String? = null,
        invoiceStatus: String? = null,
        showReviewInvoiceCta: Boolean = false,
        showDownloadInvoice: Boolean = false
    ): InvoiceStatusInfo = InvoiceStatusInfo(
        isGstVendor = false,
        ticketStatus = ticketStatus,
        invoiceStatus = invoiceStatus,
        showReviewInvoiceCta = showReviewInvoiceCta,
        showDownloadInvoice = showDownloadInvoice,
        paymentInfo = null,
        failureMessage = null
    )

    /**
     * Creates GST vendor invoice info with payment details.
     */
    fun createGstVendorInvoiceInfoWithPayment(
        ticketStatus: String? = null,
        invoiceStatus: String? = null,
        failureMessage: String? = null,
        paymentTimestamp: String? = null,
        utr: String? = null,
        amount: Double = 10000.0
    ): InvoiceStatusInfo = InvoiceStatusInfo(
        isGstVendor = true,
        ticketStatus = ticketStatus,
        invoiceStatus = invoiceStatus,
        showReviewInvoiceCta = false,
        showDownloadInvoice = false,
        paymentInfo = InvoicePaymentInfo(
            paymentTimestamp = paymentTimestamp,
            utr = utr,
            amount = amount
        ),
        failureMessage = failureMessage
    )

    // ==================== StatusUpdateInfo ====================

    /**
     * Creates status update info with configurable milestone completion.
     */
    fun createStatusUpdateInfo(
        arrived: Boolean = false,
        loaded: Boolean = false,
        reached: Boolean = false,
        unloaded: Boolean = false,
        completed: Boolean = false,
        arrivedTime: String = "2024-01-01T10:00:00",
        loadedTime: String = "2024-01-01T11:00:00",
        reachedTime: String = "2024-01-01T15:00:00",
        unloadedTime: String = "2024-01-01T16:00:00",
        completedTime: String = "2024-01-01T17:00:00",
        userId: String = "user1"
    ): StatusUpdateInfo = StatusUpdateInfo(
        tripConfirmedInfo = null,
        truckArrivedInfo = if (arrived) ByUser(arrivedTime, userId) else null,
        loadedInfo = if (loaded) ByUser(loadedTime, userId) else null,
        inTransitInfo = null,
        truckReachedInfo = if (reached) ByUser(reachedTime, userId) else null,
        truckUnloadedInfo = if (unloaded) ByUser(unloadedTime, userId) else null,
        epodUploadInfo = null,
        tripCompletedInfo = if (completed) ByUser(completedTime, userId) else null
    )

    /**
     * Creates a fully completed trip status update info.
     */
    fun createCompletedStatusUpdateInfo(
        userId: String = "user1"
    ): StatusUpdateInfo = createStatusUpdateInfo(
        arrived = true,
        loaded = true,
        reached = true,
        unloaded = true,
        completed = true,
        userId = userId
    )

    // ==================== InvoicePaymentInfo ====================

    /**
     * Creates payment info for paid invoices.
     */
    fun createPaymentInfo(
        paymentTimestamp: String = "2024-01-15T14:30:00",
        utr: String = "UTR123456789",
        amount: Double = 10000.0
    ): InvoicePaymentInfo = InvoicePaymentInfo(
        paymentTimestamp = paymentTimestamp,
        utr = utr,
        amount = amount
    )

    // ==================== Preset Scenarios ====================

    /**
     * Creates a trip with GST vendor that has completed all milestones and is paid.
     */
    fun createPaidGstVendorTrip(): HomeTripsItemData = createBaseTripDetails(
        tripStatus = "trip_completed",
        isSettled = true,
        updateInfo = createCompletedStatusUpdateInfo(),
        invoiceStatusInfo = createGstVendorInvoiceInfoWithPayment(
            ticketStatus = "paid",
            invoiceStatus = "paid",
            paymentTimestamp = "2024-01-15T14:30:00",
            utr = "UTR123456789"
        )
    )

    /**
     * Creates a trip with payment failure.
     */
    fun createPaymentFailedTrip(
        failureMessage: String = "Insufficient funds"
    ): HomeTripsItemData = createBaseTripDetails(
        tripStatus = "trip_completed",
        updateInfo = createCompletedStatusUpdateInfo(),
        invoiceStatusInfo = createGstVendorInvoiceInfo(
            ticketStatus = "closed",
            invoiceStatus = "payment_failed",
            failureMessage = failureMessage
        )
    )

    /**
     * Creates a trip pending invoice review.
     */
    fun createPendingInvoiceReviewTrip(): HomeTripsItemData = createBaseTripDetails(
        tripStatus = "trip_completed",
        updateInfo = createCompletedStatusUpdateInfo(),
        invoiceStatusInfo = createGstVendorInvoiceInfo(
            ticketStatus = "closed",
            invoiceStatus = "invoice_under_review",
            showReviewInvoiceCta = true
        )
    )

    /**
     * Creates a non-GST vendor trip that is paid.
     */
    fun createPaidNonGstVendorTrip(): HomeTripsItemData = createBaseTripDetails(
        tripStatus = "trip_completed",
        isSettled = true,
        updateInfo = createCompletedStatusUpdateInfo(),
        invoiceStatusInfo = createNonGstVendorInvoiceInfo(
            ticketStatus = "paid",
            invoiceStatus = "paid"
        )
    )
}

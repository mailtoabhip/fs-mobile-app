package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.api.response.InvoiceStatus
import com.delhivery.axle.api.response.PaymentStatus
import com.delhivery.axle.data.home.trips.InvoiceStatusInfo

/**
 * CTA action types for trip details
 */
enum class TripCtaAction {
    REFRESH,
    REVIEW,
    DOWNLOAD,
    NONE
}

/**
 * Configuration for CTA (Call-to-Action button) on trip details screen.
 * Single button with dynamic text and action.
 */
data class TripCtaConfig(
    val action: TripCtaAction = TripCtaAction.NONE,
    val buttonText: String = "",
    val visible: Boolean = true
) {
    val isVisible: Boolean get() = action != TripCtaAction.NONE && visible
}

/**
 * Provider for CTA configuration based on vendor type and trip status.
 * 
 * CTA Logic:
 * - If showDownloadInvoice = true -> Show "Download Invoice"
 * - If showReviewInvoiceCta = true -> Show "Review Invoice"
 * - If both false or null -> Show "Refresh"
 */
object TripCtaProvider {
    fun getAdhocIntracityCtaConfig(
        invoiceStatusInfo: InvoiceStatusInfo?
    ): TripCtaConfig {
        return when {
            invoiceStatusInfo == null ->
                TripCtaConfig(action = TripCtaAction.REFRESH, buttonText = "Refresh")

            invoiceStatusInfo.showDownloadInvoice ->
                TripCtaConfig(action = TripCtaAction.DOWNLOAD, buttonText = "Download Invoice")

            invoiceStatusInfo.showReviewInvoiceCta ->
                TripCtaConfig(action = TripCtaAction.REVIEW, buttonText = "Review Invoice")

            else -> TripCtaConfig(action = TripCtaAction.REFRESH, buttonText = "Refresh")
        }
    }
}

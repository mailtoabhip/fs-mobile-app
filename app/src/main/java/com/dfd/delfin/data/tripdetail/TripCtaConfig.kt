package com.dfd.delfin.data.tripdetail

import androidx.annotation.StringRes
import com.dfd.delfin.R
import com.dfd.delfin.data.home.trips.InvoiceStatusInfo

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
 * Uses StringRes for button text to support localization.
 */
data class TripCtaConfig(
    val action: TripCtaAction = TripCtaAction.NONE,
    @StringRes val buttonTextRes: Int = R.string.cta_refresh,
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
                TripCtaConfig(action = TripCtaAction.REFRESH, buttonTextRes = R.string.cta_refresh)

            invoiceStatusInfo.showDownloadInvoice ->
                TripCtaConfig(action = TripCtaAction.DOWNLOAD, buttonTextRes = R.string.cta_download_invoice)

            invoiceStatusInfo.showReviewInvoiceCta ->
                TripCtaConfig(action = TripCtaAction.REVIEW, buttonTextRes = R.string.cta_review_invoice)

            else ->
                TripCtaConfig(action = TripCtaAction.REFRESH, buttonTextRes = R.string.cta_refresh)
        }
    }
}

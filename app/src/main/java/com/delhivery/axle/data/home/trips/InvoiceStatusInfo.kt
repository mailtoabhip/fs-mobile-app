package com.delhivery.axle.data.home.trips

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Invoice status information for GST vendors
 */
data class InvoiceStatusInfo(
    @SerializedName("is_gst_vendor") val isGstVendor: Boolean,
    @SerializedName("ticket_status") val ticketStatus: String?,//attended, closed, paid
    @SerializedName("invoice_status") val invoiceStatus: String?,//(invoiced, paid, payment_failed, under_finance_review)
    @SerializedName("show_review_invoice_cta") val showReviewInvoiceCta: Boolean,
    @SerializedName("show_download_invoice") val showDownloadInvoice: Boolean,
    @SerializedName("payment_info") val paymentInfo: InvoicePaymentInfo?,
    @SerializedName("failure_message") val failureMessage: String?
) : Serializable

/**
 * Payment information for settled invoices
 */
data class InvoicePaymentInfo(
    @SerializedName("payment_timestamp") val paymentTimestamp: String?,
    @SerializedName("utr") val utr: String?,
    @SerializedName("amount") val amount: Double?
) : Serializable


enum class TicketStatus(val value: String) {
    ATTENDED("attended"),
    CLOSED("closed"),
    PAID("paid");

    companion object {
        fun fromValue(value: String?): TicketStatus? {
            return entries.firstOrNull {
                it.value.equals(value, ignoreCase = true)
            }
        }
    }
}

enum class TripInvoiceStatus(val value: String) {
    INVOICED("invoiced"),
    ACCEPTED("accepted"),
    PAID("paid"),
    PAYMENT_FAILED("payment_failed"),
    UNDER_FINANCE_REVIEW("under_finance_review"),
    INVOICE_UNDER_REVIEW("invoice_under_review");

    companion object {
        fun fromValue(value: String?): TripInvoiceStatus? {
            return entries.firstOrNull {
                it.value.equals(value, ignoreCase = true)
            }
        }
    }
}



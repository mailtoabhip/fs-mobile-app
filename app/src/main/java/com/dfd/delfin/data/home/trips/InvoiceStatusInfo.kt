package com.dfd.delfin.data.home.trips

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Invoice status information for GST vendors
 */
data class InvoiceStatusInfo(
    @SerializedName("is_gst_vendor") val isGstVendor: Boolean? = null,
    @SerializedName("ticket_status") val ticketStatus: String? = null,//attended, closed, paid
    @SerializedName("invoice_status") val invoiceStatus: String? = null,//(invoiced, paid, payment_failed, under_finance_review)
    @SerializedName("show_review_invoice_cta") val showReviewInvoiceCta: Boolean = false,
    @SerializedName("show_download_invoice") val showDownloadInvoice: Boolean = false,
    @SerializedName("payment_info") val paymentInfo: InvoicePaymentInfo? = null,
    @SerializedName("failure_message") val failureMessage: String? = null
) : Serializable

/**
 * Payment information for settled invoices
 */
data class InvoicePaymentInfo(
    @SerializedName("payment_timestamp") val paymentTimestamp: String? = null,
    @SerializedName("utr") val utr: String? = null,
    @SerializedName("amount") val amount: Double? = null
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
    CREATED("created"),//only for non gst case
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



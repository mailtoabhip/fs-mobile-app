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
) : Serializable {

    companion object {
        /**
         * GST Vendor - Ticket Attended, Invoice pending review (show Review CTA)
         */
        fun dummyGstVendorInvoicePendingReview(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = true,
                ticketStatus = "closed",
                invoiceStatus = "invoiced",
                showReviewInvoiceCta = true,
                showDownloadInvoice = false,
                paymentInfo = null,
                failureMessage = null
            )
        }

        /**
         * GST Vendor - Ticket Closed, Invoice under finance review
         */
        fun dummyGstVendorInvoiceUnderFinanceReview(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = true,
                ticketStatus = "closed",
                invoiceStatus = "under_finance_review",
                showReviewInvoiceCta = false,
                showDownloadInvoice = false,
                paymentInfo = null,
                failureMessage = null
            )
        }

        /**
         * GST Vendor - Payment completed (show Download CTA)
         */
        fun dummyGstVendorPaymentCompleted(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = true,
                ticketStatus = "paid",
                invoiceStatus = "paid",
                showReviewInvoiceCta = false,
                showDownloadInvoice = true,
                paymentInfo = InvoicePaymentInfo(
                    paymentTimestamp = "2026-03-17T14:30:00",
                    utr = "UTR123456789",
                    amount = 7493.0
                ),
                failureMessage = null
            )
        }

        /**
         * GST Vendor - Payment failed
         */
        fun dummyGstVendorPaymentFailed(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = true,
                ticketStatus = "closed",
                invoiceStatus = "payment_failed",
                showReviewInvoiceCta = false,
                showDownloadInvoice = true,
                paymentInfo = null,
                failureMessage = "Payment failed due to insufficient funds"
            )
        }

        /**
         * Non-GST Vendor - Ticket Attended, waiting for closure
         */
        fun dummyNonGstVendorTicketAttended(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = false,
                ticketStatus = "attended",
                invoiceStatus = null,
                showReviewInvoiceCta = false,
                showDownloadInvoice = false,
                paymentInfo = null,
                failureMessage = null
            )
        }

        /**
         * Non-GST Vendor - Ticket Closed, payment pending
         */
        fun dummyNonGstVendorTicketClosed(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = false,
                ticketStatus = "closed",
                invoiceStatus = "invoiced",
                showReviewInvoiceCta = false,
                showDownloadInvoice = false,
                paymentInfo = null,
                failureMessage = null
            )
        }

        /**
         * Non-GST Vendor - Payment completed (show Download CTA)
         */
        fun dummyNonGstVendorPaymentCompleted(): InvoiceStatusInfo {
            return InvoiceStatusInfo(
                isGstVendor = false,
                ticketStatus = "paid",
                invoiceStatus = "paid",
                showReviewInvoiceCta = false,
                showDownloadInvoice = true,
                paymentInfo = InvoicePaymentInfo(
                    paymentTimestamp = "2026-03-18T10:15:00",
                    utr = "UTR987654321",
                    amount = 5000.0
                ),
                failureMessage = null
            )
        }

        /**
         * Get dummy InvoiceStatusInfo based on milestone step (for testing different states)
         *
         * GST Vendor (7-step flow):
         * Step 1-3: Trip milestones (no invoice info yet)
         * Step 4: Ticket Closed
         * Step 5: Accept Invoice / Billing Under Review
         * Step 6: Invoice Accepted
         * Step 7: Payment Released
         *
         * Non-GST Vendor (5-step flow):
         * Step 1-3: Trip milestones (no invoice info yet)
         * Step 4: Ticket Closed
         * Step 5: Payment Released
         *
         * @param isGstVendor Whether vendor is GST registered
         * @param step Milestone step (1-7 for GST, 1-5 for non-GST)
         * @return InvoiceStatusInfo for the given step
         */
        fun dummyForStep(isGstVendor: Boolean, step: Int): InvoiceStatusInfo {
            return if (isGstVendor) {
                when (step) {
                    1, 2, 3 -> InvoiceStatusInfo(
                        isGstVendor = true,
                        ticketStatus = null,
                        invoiceStatus = null,
                        showReviewInvoiceCta = false,
                        showDownloadInvoice = false,
                        paymentInfo = null,
                        failureMessage = null
                    )
                    4 -> dummyGstVendorInvoicePendingReview()
                    5 -> dummyGstVendorInvoicePendingReview()
                    6 -> dummyGstVendorInvoiceUnderFinanceReview()
                    7 -> dummyGstVendorPaymentCompleted()
                    else -> dummyGstVendorInvoicePendingReview()
                }
            } else {
                when (step) {
                    1, 2, 3 -> InvoiceStatusInfo(
                        isGstVendor = false,
                        ticketStatus = null,
                        invoiceStatus = null,
                        showReviewInvoiceCta = false,
                        showDownloadInvoice = false,
                        paymentInfo = null,
                        failureMessage = null
                    )
                    4 -> dummyNonGstVendorTicketClosed()
                    5 -> dummyNonGstVendorPaymentCompleted()
                    else -> dummyNonGstVendorTicketAttended()
                }
            }
        }
    }
}

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
    UNDER_FINANCE_REVIEW("under_finance_review");

    companion object {
        fun fromValue(value: String?): TripInvoiceStatus? {
            return entries.firstOrNull {
                it.value.equals(value, ignoreCase = true)
            }
        }
    }
}



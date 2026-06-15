package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for vendor invoice details
 */
data class InvoiceDetailsResponse(
    @SerializedName("ticket_id") val ticketId: String? = "",
    @SerializedName("orion_transaction_id") val orionTransactionId: String? = "",
    @SerializedName("status") val status: String? = "",
    @SerializedName("center_contact_number") val centerContactNumber: String? = "",

    // Billed From (Vendor)
    @SerializedName("billed_from") val billedFrom: BilledParty? = BilledParty(),

    // Billed To (Delhivery)
    @SerializedName("billed_to") val billedTo: BilledParty? = BilledParty(),

    // Invoice details
    @SerializedName("place_of_supply") val placeOfSupply: String? = "",
    @SerializedName("bank_name") val bankName: String? = "",
    @SerializedName("bank_account_number") val bankAccountNumber: String? = "",
    @SerializedName("service_description") val serviceDescription: String? = "",
    @SerializedName("sac_code") val sacCode: String? = "",

    // Invoice particulars
    @SerializedName("invoice_particulars") val invoiceParticulars: List<InvoiceParticular> = emptyList(),

    // Amounts
    @SerializedName("total_taxable_value") val totalTaxableValue: Double? =null,
    @SerializedName("sgst_amount") val sgstAmount: Double? = null,
    @SerializedName("cgst_amount") val cgstAmount: Double? = null,
    @SerializedName("igst_amount") val igstAmount: Double? = null,
    @SerializedName("gst_rate") val gstRate: Int? = null,
    @SerializedName("gst_display") val gstDisplay: String? = "",
    @SerializedName("gst_amount") val gstAmount: Double? = null,
    @SerializedName("grand_total") val grandTotal: Double? = null
) {
    // Helper to get invoice status enum
    fun getInvoiceStatus(): InvoiceStatus {
        return when (status) {
            "invoice_under_review" -> InvoiceStatus.PENDING_REVIEW
            "invoice_accepted", "accepted" -> InvoiceStatus.ACCEPTED
            "invoice_rejected", "rejected" -> InvoiceStatus.REJECTED
            else -> InvoiceStatus.PENDING_REVIEW
        }
    }
}

/**
 * Billed party (from/to) details
 */
data class BilledParty(
    @SerializedName("name") val name: String? = "",
    @SerializedName("address") val address: String? = "",
    @SerializedName("gstin") val gstin: String? = ""
)

/**
 * Invoice line item
 */
data class InvoiceParticular(
    @SerializedName("description") val description: String? = "",
    @SerializedName("amount") val amount: Double? = null
)

/**
 * Invoice status enum
 */
enum class InvoiceStatus {
    @SerializedName("pending_review") PENDING_REVIEW,
    @SerializedName("invoice_under_review") INVOICE_UNDER_REVIEW,
    @SerializedName("accepted") ACCEPTED,
    @SerializedName("rejected") REJECTED
}

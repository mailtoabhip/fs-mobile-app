package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for invoice accept/reject action
 * 
 * API: POST /ticket_invoices/action
 * 
 * On accept: generates invoice PDF, pushes to Oracle, updates Orion to invoiced
 * On reject: updates status to rejected, updates Orion to trip_completed
 */
data class InvoiceActionRequest(
    @SerializedName("ticket_id")
    val ticketId: String,
    
    @SerializedName("action")
    val action: String,
    
    @SerializedName("vendor_invoice_number")
    val vendorInvoiceNumber: String? = null,
    
    @SerializedName("vendor_invoice_date")
    val vendorInvoiceDate: String? = null
)

package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for invoice accept/reject action
 * 
 * API: POST /ticket_invoices/action
 */
data class InvoiceActionResponse(
    @SerializedName("ticket_id")
    val ticketId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String?
)

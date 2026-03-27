package com.delhivery.axle.testdata

import com.delhivery.axle.api.response.BilledParty
import com.delhivery.axle.api.response.InvoiceActionResponse
import com.delhivery.axle.api.response.InvoiceDetailsResponse
import com.delhivery.axle.api.response.InvoiceParticular
import com.google.gson.Gson

/**
 * Factory for creating test data objects for Invoice-related tests.
 * Supports both direct object creation and JSON parsing.
 */
object InvoiceTestDataFactory {

    private val gson = Gson()

    // ==================== InvoiceDetailsResponse ====================

    fun createInvoiceDetails(
        ticketId: String = "TICKET123",
        orionTransactionId: String = "TXN123",
        status: String = "invoice_under_review",
        centerContactNumber: String = "9876543210",
        billedFrom: BilledParty = createBilledParty(name = "Vendor"),
        billedTo: BilledParty = createBilledParty(name = "Delhivery"),
        placeOfSupply: String = "Delhi",
        bankName: String = "Test Bank",
        bankAccountNumber: String = "1234567890",
        serviceDescription: String = "Transport",
        sacCode: String? = "996511",
        invoiceParticulars: List<InvoiceParticular> = emptyList(),
        totalTaxableValue: Double = 1000.0,
        sgstAmount: Double? = null,
        cgstAmount: Double? = null,
        igstAmount: Double? = 180.0,
        gstRate: Int? = 18,
        gstDisplay: String? = "IGST @18%",
        gstAmount: Double? = 180.0,
        grandTotal: Double = 1180.0
    ): InvoiceDetailsResponse = InvoiceDetailsResponse(
        ticketId = ticketId,
        orionTransactionId = orionTransactionId,
        status = status,
        centerContactNumber = centerContactNumber,
        billedFrom = billedFrom,
        billedTo = billedTo,
        placeOfSupply = placeOfSupply,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        serviceDescription = serviceDescription,
        sacCode = sacCode,
        invoiceParticulars = invoiceParticulars,
        totalTaxableValue = totalTaxableValue,
        sgstAmount = sgstAmount,
        cgstAmount = cgstAmount,
        igstAmount = igstAmount,
        gstRate = gstRate,
        gstDisplay = gstDisplay,
        gstAmount = gstAmount,
        grandTotal = grandTotal
    )

    fun createInvoiceDetailsFromJson(json: String): InvoiceDetailsResponse =
        gson.fromJson(json, InvoiceDetailsResponse::class.java)

    // ==================== InvoiceActionResponse ====================

    fun createInvoiceActionResponse(
        ticketId: String = "TICKET123",
        status: String = "accepted",
        message: String? = "Invoice processed successfully"
    ): InvoiceActionResponse = InvoiceActionResponse(
        ticketId = ticketId,
        status = status,
        message = message
    )

    fun createAcceptedResponse(
        ticketId: String = "TICKET123",
        message: String = "Invoice accepted successfully"
    ): InvoiceActionResponse = createInvoiceActionResponse(
        ticketId = ticketId,
        status = "accepted",
        message = message
    )

    fun createRejectedResponse(
        ticketId: String = "TICKET123",
        message: String = "Invoice rejected"
    ): InvoiceActionResponse = createInvoiceActionResponse(
        ticketId = ticketId,
        status = "rejected",
        message = message
    )

    fun createInvoiceActionResponseFromJson(json: String): InvoiceActionResponse =
        gson.fromJson(json, InvoiceActionResponse::class.java)

    // ==================== BilledParty ====================

    fun createBilledParty(
        name: String = "Test Party",
        address: String = "Test Address",
        gstin: String? = "GSTIN123456"
    ): BilledParty = BilledParty(
        name = name,
        address = address,
        gstin = gstin
    )

    // ==================== InvoiceParticular ====================

    fun createInvoiceParticular(
        description: String = "Service Charge",
        amount: Double = 500.0
    ): InvoiceParticular = InvoiceParticular(
        description = description,
        amount = amount
    )

    // ==================== Sample JSON Strings ====================

    object SampleJson {
        val invoiceDetails = """
            {
                "ticket_id": "TICKET123",
                "orion_transaction_id": "TXN123",
                "status": "invoice_under_review",
                "center_contact_number": "9876543210",
                "billed_from": {
                    "name": "Vendor",
                    "address": "Vendor Address",
                    "gstin": "GSTIN123"
                },
                "billed_to": {
                    "name": "Delhivery",
                    "address": "Delhivery Address",
                    "gstin": "GSTIN456"
                },
                "place_of_supply": "Delhi",
                "bank_name": "Test Bank",
                "bank_account_number": "1234567890",
                "service_description": "Transport",
                "sac_code": "996511",
                "invoice_particulars": [],
                "total_taxable_value": 1000.0,
                "igst_amount": 180.0,
                "gst_rate": 18,
                "gst_display": "IGST @18%",
                "gst_amount": 180.0,
                "grand_total": 1180.0
            }
        """.trimIndent()

        val acceptedActionResponse = """
            {
                "ticket_id": "TICKET123",
                "status": "accepted",
                "message": "Invoice accepted successfully"
            }
        """.trimIndent()

        val rejectedActionResponse = """
            {
                "ticket_id": "TICKET123",
                "status": "rejected",
                "message": "Invoice rejected"
            }
        """.trimIndent()
    }
}

package com.delhivery.axle.ui.invoicereview

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.InvoiceRepository
import com.delhivery.axle.api.request.InvoiceActionRequest
import com.delhivery.axle.api.response.BilledParty
import com.delhivery.axle.api.response.InvoiceDetailsResponse
import com.delhivery.axle.api.response.InvoiceParticular
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * ViewModel for Invoice Review screen
 */
class InvoiceReviewViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : BaseViewModel() {

    // Transaction ID for the invoice
    var transactionId: String = ""

    // Invoice details LiveData
    val invoiceDetailsLiveData = MutableLiveData<InvoiceDetailsResponse>()

    // Validation errors LiveData
    val invoiceNumberErrorLiveData = MutableLiveData<String?>()
    val invoiceDateErrorLiveData = MutableLiveData<String?>()

    // Accept/Reject result LiveData
    val invoiceAcceptedLiveData = MutableLiveData<Boolean>()
    val invoiceRejectedLiveData = MutableLiveData<Boolean>()

    // Error LiveData
    val errorLiveData = MutableLiveData<String>()

    // Loading state
    val isLoadingLiveData = MutableLiveData<Boolean>()

    // Already processed flag
    val alreadyProcessedLiveData = MutableLiveData<Boolean>()

    // Current invoice details (cached for retry)
    private var currentInvoice: InvoiceDetailsResponse? = null

    /**
     * Fetch invoice details from backend
     */
    fun fetchInvoiceDetails() {
        if (transactionId.isEmpty()) {
            errorLiveData.postValue("Transaction ID is required")
            return
        }

        isLoadingLiveData.postValue(true)
        
        // TODO: Replace with actual API call when ready
        // Using dummy data for testing
        val dummyResponse = InvoiceDetailsResponse(
            ticketId = "fms::ticket::abc123",
            orionTransactionId = transactionId,
            status = "invoice_under_review",
            billedFrom = BilledParty(
                name = "FLT D K Transport Service",
                address = "RAJKOT, Gujarat",
                gstin = "24BADPN5467Q1Z2"
            ),
            billedTo = BilledParty(
                name = "DELHIVERY LIMITED",
                address = "SURAT, Gujarat",
                gstin = "24AAPCS9575E1ZT"
            ),
            placeOfSupply = "Gujarat",
            bankName = "Dk logistics",
            bankAccountNumber = "312213123132312",
            serviceDescription = "Adhoc Vehicle Rental",
            sacCode = "996601",
            invoiceParticulars = listOf(
                InvoiceParticular(
                    description = "Adhoc Vehicle Rental on 12-Sep-2025, Vehicle No. -2313",
                    amount = 685.00
                )
            ),
            totalTaxableValue = 685.00,
            sgstAmount = 41.00,
            cgstAmount = 41.00,
            igstAmount = 0.00,
            gstRate = 18,
            gstDisplay = "SGST 9% + CGST 9%",
            gstAmount = 82.00,
            grandTotal = 767.00
        )
        
        isLoadingLiveData.postValue(false)
        currentInvoice = dummyResponse
        invoiceDetailsLiveData.postValue(dummyResponse)
        
        compositeDisposable += invoiceRepository.getInvoiceDetails(transactionId)
            .onBackground()
            .subscribe({ response ->
                isLoadingLiveData.postValue(false)
                currentInvoice = response
                invoiceDetailsLiveData.postValue(response)
            }, { error ->
                isLoadingLiveData.postValue(false)
                error.handle()
                errorLiveData.postValue(error.message ?: "Failed to fetch invoice details")
            })
    }

    /**
     * Validate invoice number input
     * @return true if valid, false otherwise
     */
    fun validateInvoiceNumber(invoiceNumber: String): Boolean {
        return when {
            invoiceNumber.isEmpty() -> {
                invoiceNumberErrorLiveData.postValue("Invoice number is required")
                false
            }
            !invoiceNumber.matches(Regex("^[a-zA-Z0-9]+$")) -> {
                invoiceNumberErrorLiveData.postValue("Invoice number must be alphanumeric")
                false
            }
            invoiceNumber.length > 50 -> {
                invoiceNumberErrorLiveData.postValue("Invoice number cannot exceed 50 characters")
                false
            }
            else -> {
                invoiceNumberErrorLiveData.postValue(null)
                true
            }
        }
    }


    /**
     * Validate all inputs
     * @return true if all inputs are valid
     */
    fun validateInputs(invoiceNumber: String, invoiceDate: String): Boolean {
        val isNumberValid = validateInvoiceNumber(invoiceNumber)
        return isNumberValid
    }

    /**
     * Accept the invoice with vendor invoice number and date
     */
    fun acceptRejectInvoice(confirmationType : InvoiceConfirmationDialog.ConfirmationType, invoiceNumber: String?, invoiceDate: String?) {
        val invoice = currentInvoice
        if (invoice == null) {
            errorLiveData.postValue("Invoice details not loaded")
            return
        }
        if(confirmationType.name == InvoiceConfirmationDialog.ConfirmationType.ACCEPT.name && (invoiceNumber.isNotNullOrEmpty() || invoiceDate.isNotNullOrEmpty())){
            if(invoiceNumber.isNotNullOrEmpty()) errorLiveData.postValue("Please enter invoice number")
            else errorLiveData.postValue("Please enter invoice date")
            return
        }

        val request = InvoiceActionRequest(
            ticketId = transactionId,
            action = confirmationType.name,
            vendorInvoiceNumber = invoiceNumber,
            vendorInvoiceDate = invoiceDate
        )

        isLoadingLiveData.postValue(true)
        compositeDisposable += invoiceRepository.invoiceAction(request)
            .onBackground()
            .progress()
            .subscribe{ response, error ->
                if (!error) {
                    isLoadingLiveData.postValue(false)
                    if(confirmationType.name == InvoiceConfirmationDialog.ConfirmationType.ACCEPT.name) invoiceAcceptedLiveData.postValue(true)
                    else invoiceRejectedLiveData.postValue(true)
                }else {
                    error.handle()
                }
            }
    }
    /**
     * Get current invoice details
     */
    fun getCurrentInvoice(): InvoiceDetailsResponse? = currentInvoice
}

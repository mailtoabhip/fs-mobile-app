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

    companion object {
        private val INVALID_CHARS_REGEX = Regex("[ _@.]")
        private const val MAX_INVOICE_NUMBER_LENGTH = 16
    }
    var transactionId: String = ""

    // Invoice details LiveData
    val invoiceDetailsLiveData = MutableLiveData<InvoiceDetailsResponse>()

    // Validation errors LiveData
    val invoiceNumberErrorLiveData = MutableLiveData<String?>()

    // Accept/Reject result LiveData
    val invoiceActionResponseMsgLiveData = MutableLiveData<String>()

    // Error LiveData
    val errorLiveData = MutableLiveData<String>()

    // Loading state
    val isLoadingLiveData = MutableLiveData<Boolean>()

    // Fetch error state (for showing retry button)
    val fetchErrorLiveData = MutableLiveData<Boolean>()

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
        fetchErrorLiveData.postValue(false)
        
        compositeDisposable += invoiceRepository.getInvoiceDetails(transactionId)
            .onBackground()
            .subscribe({ response ->
                isLoadingLiveData.postValue(false)
                currentInvoice = response
                invoiceDetailsLiveData.postValue(response)
            }, { error ->
                isLoadingLiveData.postValue(false)
                error.handle()
            })
    }

    /**
     * Validate invoice number input
     * @return true if valid, false otherwise
     */
    fun validateInvoiceNumber(invoiceNumber: String, isRejectCase: Boolean): Boolean {
        // Skip validation entirely for reject case with empty invoice number
        if (invoiceNumber.isEmpty() && isRejectCase) {
            invoiceNumberErrorLiveData.postValue(null)
            return true
        }
        return when {
            invoiceNumber.isEmpty() -> {
                invoiceNumberErrorLiveData.postValue("Invoice number is required")
                false
            }
            INVALID_CHARS_REGEX.containsMatchIn(invoiceNumber) -> {
                invoiceNumberErrorLiveData.postValue("Invoice number must not contain _, @, . or space")
                false
            }
            invoiceNumber.length > MAX_INVOICE_NUMBER_LENGTH -> {
                invoiceNumberErrorLiveData.postValue("Invoice number cannot exceed $MAX_INVOICE_NUMBER_LENGTH characters")
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
    fun validateInputs(invoiceNumber: String, isRejectCase:Boolean): Boolean {
        val isNumberValid = validateInvoiceNumber(invoiceNumber,isRejectCase)
        return isNumberValid
    }

    /**
     * Accept the invoice with vendor invoice number and date
     */
    fun acceptRejectInvoice(confirmationType : InvoiceConfirmationDialog.ConfirmationType, invoiceNumber: String?, invoiceDate: String?) {
        val invoice = currentInvoice
        val ticketId = invoice?.ticketId
        if (invoice == null) {
            errorLiveData.postValue("Invoice details not loaded")
            return
        }
        if (ticketId.isNullOrEmpty()) {
            errorLiveData.postValue("Invoice ticketId missing")
            return
        }

        val isAcceptAction = confirmationType == InvoiceConfirmationDialog.ConfirmationType.ACCEPT

        if (isAcceptAction && (invoiceNumber.isNullOrEmpty() || invoiceDate.isNullOrEmpty())) {
            if (invoiceNumber.isNullOrEmpty()) errorLiveData.postValue("Please enter invoice number")
            else errorLiveData.postValue("Please enter invoice date")
            return
        }

        // Sanitize inputs - trim and remove any potentially harmful characters
        val sanitizedInvoiceNumber = invoiceNumber?.trim()?.take(MAX_INVOICE_NUMBER_LENGTH)
        val sanitizedInvoiceDate = invoiceDate?.trim()

        val request = InvoiceActionRequest(
            ticketId = ticketId,
            action = confirmationType.value,
            vendorInvoiceNumber = sanitizedInvoiceNumber,
            vendorInvoiceDate = sanitizedInvoiceDate,
            rejectionRemark = if(!isAcceptAction) "Rejected by Vendor" else null
        )

        isLoadingLiveData.postValue(true)
        compositeDisposable += invoiceRepository.invoiceAction(request)
            .onBackground()
            .progress()
            .subscribe{ response, error ->
                isLoadingLiveData.postValue(false)
                if (!error) {
                    invoiceActionResponseMsgLiveData.postValue(response.message?:"Success")
                }else {
                    error.handle()
                }
            }
    }

}

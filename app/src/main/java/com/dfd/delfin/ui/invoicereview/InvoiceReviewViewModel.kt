package com.dfd.delfin.ui.invoicereview

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.InvoiceRepository
import com.dfd.delfin.api.request.InvoiceActionRequest
import com.dfd.delfin.api.response.InvoiceDetailsResponse
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.plusAssign
import javax.inject.Inject

/**
 * Error types for Invoice Review - Activity resolves to string resources
 */
enum class InvoiceReviewErrorType {
    TRANSACTION_ID_REQUIRED,
    INVOICE_NUMBER_REQUIRED,
    INVOICE_NUMBER_INVALID_CHARS,
    INVOICE_NUMBER_MAX_LENGTH,
    INVOICE_DETAILS_NOT_LOADED,
    INVOICE_TICKET_ID_MISSING,
    ENTER_INVOICE_NUMBER,
    ENTER_INVOICE_DATE,
    ALREADY_PROCESSED
}

enum class ConfirmationType(val value: String) {
    ACCEPT("accept"),
    REJECT("reject")
}

/**
 * ViewModel for Invoice Review screen
 */
class InvoiceReviewViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : BaseViewModel() {

    companion object {
        private val INVALID_CHARS_REGEX = Regex("[ _@.]")
        const val MAX_INVOICE_NUMBER_LENGTH = 16
    }

    var fmsTicketId: String = ""
    var centerContactNumber: String = ""

    // Invoice details LiveData
    val invoiceDetailsLiveData = MutableLiveData<InvoiceDetailsResponse>()

    // Validation errors LiveData - uses enum
    val invoiceNumberErrorLiveData = MutableLiveData<InvoiceReviewErrorType?>()

    // Accept/Reject result LiveData
    val invoiceActionResponseMsgLiveData = MutableLiveData<String>()

    // Error LiveData - uses enum
    val errorLiveData = MutableLiveData<InvoiceReviewErrorType>()

    // Loading state
    val isLoadingLiveData = MutableLiveData<Boolean>()

    // Fetch error state (for showing retry button)
    val fetchErrorLiveData = MutableLiveData<Boolean>()

    // Current invoice details (cached for retry)
    private var currentInvoice: InvoiceDetailsResponse? = null

    /**
     * Fetch invoice details from backend
     */
    fun fetchInvoiceDetails() {
        if (fmsTicketId.isEmpty()) {
            errorLiveData.postValue(InvoiceReviewErrorType.TRANSACTION_ID_REQUIRED)
            return
        }

        isLoadingLiveData.postValue(true)
        fetchErrorLiveData.postValue(false)

        compositeDisposable += invoiceRepository.getInvoiceDetails(fmsTicketId)
            .onBackground()
            .progress()
            .subscribe { response, error ->
                isLoadingLiveData.postValue(false)
                if (!error) {
                    currentInvoice = response
                    currentInvoice?.centerContactNumber?.let{centerContactNumber = it}
                    invoiceDetailsLiveData.postValue(response)
                } else {
                    error.handle()
                }
            }
    }

    /**
     * Validate invoice number input
     * @return true if valid, false otherwise
     */
    fun validateInvoiceNumber(invoiceNumber: String, isRejectCase: Boolean): Boolean {
        // Skip validation for reject case with empty invoice number
        if (invoiceNumber.isEmpty() && isRejectCase) {
            invoiceNumberErrorLiveData.postValue(null)
            return true
        }
        return when {
            invoiceNumber.isEmpty() -> {
                invoiceNumberErrorLiveData.postValue(InvoiceReviewErrorType.INVOICE_NUMBER_REQUIRED)
                false
            }
            INVALID_CHARS_REGEX.containsMatchIn(invoiceNumber) -> {
                invoiceNumberErrorLiveData.postValue(InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS)
                false
            }
            invoiceNumber.length > MAX_INVOICE_NUMBER_LENGTH -> {
                invoiceNumberErrorLiveData.postValue(InvoiceReviewErrorType.INVOICE_NUMBER_MAX_LENGTH)
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
    fun validateInputs(invoiceNumber: String, isRejectCase: Boolean): Boolean {
        return validateInvoiceNumber(invoiceNumber, isRejectCase)
    }


    /**
     * Accept or reject the invoice with vendor invoice number and date
     */
    fun acceptRejectInvoice(
        confirmationType: ConfirmationType,
        invoiceNumber: String?,
        invoiceDate: String?
    ) {
        val invoice = currentInvoice
        val ticketId = invoice?.ticketId

        if (invoice == null) {
            errorLiveData.postValue(InvoiceReviewErrorType.INVOICE_DETAILS_NOT_LOADED)
            return
        }
        if (ticketId.isNullOrEmpty()) {
            errorLiveData.postValue(InvoiceReviewErrorType.INVOICE_TICKET_ID_MISSING)
            return
        }

        val isAcceptAction = confirmationType == ConfirmationType.ACCEPT

        if (isAcceptAction && (invoiceNumber.isNullOrEmpty() || invoiceDate.isNullOrEmpty())) {
            if (invoiceNumber.isNullOrEmpty()) {
                errorLiveData.postValue(InvoiceReviewErrorType.ENTER_INVOICE_NUMBER)
            } else {
                errorLiveData.postValue(InvoiceReviewErrorType.ENTER_INVOICE_DATE)
            }
            return
        }

        // Sanitize inputs
        val sanitizedInvoiceNumber = invoiceNumber?.trim()?.take(MAX_INVOICE_NUMBER_LENGTH)
        val sanitizedInvoiceDate = invoiceDate?.trim()

        val request = InvoiceActionRequest(
            ticketId = ticketId,
            action = confirmationType.value,
            vendorInvoiceNumber = sanitizedInvoiceNumber,
            vendorInvoiceDate = sanitizedInvoiceDate,
            rejectionRemark = if(!isAcceptAction) "Rejected by Vendor" else null //hardcoded for now
        )

        isLoadingLiveData.postValue(true)
        compositeDisposable += invoiceRepository.invoiceAction(request)
            .onBackground()
            .progress()
            .subscribe { response, error ->
                isLoadingLiveData.postValue(false)
                if (!error) {
                    invoiceActionResponseMsgLiveData.postValue(response.message ?: "")
                } else {
                    error.handle()
                }
            }
    }
}

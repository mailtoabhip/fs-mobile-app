package com.delhivery.axle.ui.invoicereview

import com.delhivery.axle.api.repository.InvoiceRepository
import com.delhivery.axle.api.request.InvoiceActionRequest
import com.delhivery.axle.testdata.InvoiceTestDataFactory
import com.delhivery.axle.testutils.ViewModelTestSetup
import com.delhivery.axle.testutils.captureValues
import com.delhivery.axle.testutils.relaxedMock
import com.delhivery.axle.testutils.strictMock
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Single
import java.net.SocketTimeoutException

/**
 * Unit tests for InvoiceReviewViewModel using Kotest and MockK.
 * 
 * Covers:
 * - Input validation (invoice number format, length, special characters)
 * - API interactions (fetch details, accept/reject actions)
 * - Error handling (network errors, validation errors)
 * - Loading state transitions
 * - Request payload verification via argument capture
 */
class InvoiceReviewViewModelTest : FunSpec({

    lateinit var invoiceRepository: InvoiceRepository
    lateinit var viewModel: InvoiceReviewViewModel

    /**
     * Helper to load a mock invoice into the ViewModel.
     * Sets up the repository mock and triggers fetch.
     */
    fun loadMockInvoice(ticketId: String = "TICKET123") {
        viewModel.fmsTicketId = "TXN123"
        val mockResponse = InvoiceTestDataFactory.createInvoiceDetails(ticketId = ticketId)
        every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.just(mockResponse)
        viewModel.fetchInvoiceDetails()
    }

    beforeSpec {
        ViewModelTestSetup.setup()
    }

    afterSpec {
        ViewModelTestSetup.teardown()
    }

    beforeEach {
        invoiceRepository = strictMock()
        viewModel = InvoiceReviewViewModel(invoiceRepository)
    }

    // ==================== validateInvoiceNumber Tests ====================

    context("validateInvoiceNumber") {
        
        context("empty input handling") {
            test("empty string returns error for accept case") {
                val result = viewModel.validateInvoiceNumber("", isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_REQUIRED
            }

            test("empty string allowed for reject case") {
                val result = viewModel.validateInvoiceNumber("", isRejectCase = true)

                result shouldBe true
                viewModel.invoiceNumberErrorLiveData.value.shouldBeNull()
            }
        }

        context("invalid characters") {
            test("contains space returns error") {
                val result = viewModel.validateInvoiceNumber("INV 123", isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
            }

            test("contains underscore returns error") {
                val result = viewModel.validateInvoiceNumber("INV_123", isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
            }

            test("contains at symbol returns error") {
                val result = viewModel.validateInvoiceNumber("INV@123", isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
            }

            test("contains dot returns error") {
                val result = viewModel.validateInvoiceNumber("INV.123", isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
            }
        }

        context("length validation") {
            test("exceeds max length returns error") {
                val longInvoiceNumber = "A".repeat(17)
                val result = viewModel.validateInvoiceNumber(longInvoiceNumber, isRejectCase = false)

                result shouldBe false
                viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_MAX_LENGTH
            }

            test("max length exactly is valid") {
                val maxLengthInvoice = "A".repeat(16)
                val result = viewModel.validateInvoiceNumber(maxLengthInvoice, isRejectCase = false)

                result shouldBe true
                viewModel.invoiceNumberErrorLiveData.value.shouldBeNull()
            }
        }

        context("valid inputs") {
            test("valid invoice number returns true") {
                val result = viewModel.validateInvoiceNumber("INV123456", isRejectCase = false)

                result shouldBe true
                viewModel.invoiceNumberErrorLiveData.value.shouldBeNull()
            }

            test("with hyphen is valid") {
                val result = viewModel.validateInvoiceNumber("INV-123-456", isRejectCase = false)

                result shouldBe true
                viewModel.invoiceNumberErrorLiveData.value.shouldBeNull()
            }

            test("alphanumeric only is valid") {
                val result = viewModel.validateInvoiceNumber("ABC123XYZ", isRejectCase = false)

                result shouldBe true
                viewModel.invoiceNumberErrorLiveData.value.shouldBeNull()
            }
        }

        context("property-based tests") {
            test("any string with invalid chars should fail") {
                val invalidChars = listOf(' ', '_', '@', '.')
                invalidChars.forEach { invalidChar ->
                    val input = "INV${invalidChar}123"
                    viewModel.validateInvoiceNumber(input, false) shouldBe false
                    viewModel.invoiceNumberErrorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
                }
            }

            test("any alphanumeric string with hyphen under 16 chars should pass") {
                val validInputs = listOf("A", "AB", "ABC123", "INV-2024-001", "A".repeat(16))
                validInputs.forEach { input ->
                    viewModel.validateInvoiceNumber(input, false) shouldBe true
                }
            }
        }
    }

    // ==================== fetchInvoiceDetails Tests ====================

    context("fetchInvoiceDetails") {
        
        context("validation") {
            test("empty fmsTicketId posts error") {
                viewModel.fmsTicketId = ""

                viewModel.fetchInvoiceDetails()

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.TRANSACTION_ID_REQUIRED
            }
        }

        context("success scenarios") {
            test("success updates invoiceDetailsLiveData") {
                viewModel.fmsTicketId = "TXN123"
                val mockResponse = InvoiceTestDataFactory.createInvoiceDetails()
                every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.just(mockResponse)

                viewModel.fetchInvoiceDetails()

                viewModel.invoiceDetailsLiveData.value shouldBe mockResponse
                viewModel.isLoadingLiveData.value shouldBe false
            }

            test("updates centerContactNumber from response") {
                viewModel.fmsTicketId = "TXN123"
                val mockResponse = InvoiceTestDataFactory.createInvoiceDetails(
                    centerContactNumber = "1234567890"
                )
                every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.just(mockResponse)

                viewModel.fetchInvoiceDetails()

                viewModel.centerContactNumber shouldBe "1234567890"
            }
        }

        context("loading state") {
            test("sets loading state correctly during fetch") {
                viewModel.fmsTicketId = "TXN123"
                val loadingStates = viewModel.isLoadingLiveData.captureValues()
                
                val mockResponse = InvoiceTestDataFactory.createInvoiceDetails()
                every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.just(mockResponse)

                viewModel.fetchInvoiceDetails()

                loadingStates shouldContainExactly listOf(true, false)
            }
        }

        context("error handling") {
            test("error sets exceptionLiveData") {
                viewModel.fmsTicketId = "TXN123"
                val error = RuntimeException("Network error")
                every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.error(error)

                viewModel.fetchInvoiceDetails()

                viewModel.isLoadingLiveData.value shouldBe false
                viewModel.exceptionLiveData.value shouldBe error
            }

            test("network timeout sets correct error state") {
                viewModel.fmsTicketId = "TXN123"
                val timeoutError = SocketTimeoutException("Connection timed out")
                every { invoiceRepository.getInvoiceDetails("TXN123") } returns Single.error(timeoutError)

                viewModel.fetchInvoiceDetails()

                viewModel.exceptionLiveData.value shouldBe timeoutError
                viewModel.isLoadingLiveData.value shouldBe false
            }
        }
    }

    // ==================== acceptRejectInvoice Tests ====================

    context("acceptRejectInvoice") {
        
        context("precondition validation") {
            test("no invoice loaded posts error") {
                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_DETAILS_NOT_LOADED
            }

            test("missing ticketId posts error") {
                loadMockInvoice(ticketId = "")

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.INVOICE_TICKET_ID_MISSING
            }
        }

        context("accept validation") {
            test("accept without invoice number posts error") {
                loadMockInvoice()

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, null, "2024-01-01")

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.ENTER_INVOICE_NUMBER
            }

            test("accept without invoice date posts error") {
                loadMockInvoice()

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", null)

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.ENTER_INVOICE_DATE
            }

            test("accept with empty invoice number posts error") {
                loadMockInvoice()

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "", "2024-01-01")

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.ENTER_INVOICE_NUMBER
            }

            test("accept with empty invoice date posts error") {
                loadMockInvoice()

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "")

                viewModel.errorLiveData.value shouldBe InvoiceReviewErrorType.ENTER_INVOICE_DATE
            }
        }

        context("reject validation") {
            test("reject without invoice number is allowed") {
                loadMockInvoice()
                val mockResponse = InvoiceTestDataFactory.createRejectedResponse()
                every { invoiceRepository.invoiceAction(any()) } returns Single.just(mockResponse)

                viewModel.acceptRejectInvoice(ConfirmationType.REJECT, null, null)

                viewModel.invoiceActionResponseMsgLiveData.value shouldBe "Invoice rejected"
            }

            test("reject with empty strings is allowed") {
                loadMockInvoice()
                val mockResponse = InvoiceTestDataFactory.createRejectedResponse()
                every { invoiceRepository.invoiceAction(any()) } returns Single.just(mockResponse)

                viewModel.acceptRejectInvoice(ConfirmationType.REJECT, "", "")

                viewModel.invoiceActionResponseMsgLiveData.value shouldBe "Invoice rejected"
            }
        }

        context("success scenarios") {
            test("accept success updates response") {
                loadMockInvoice()
                val mockResponse = InvoiceTestDataFactory.createAcceptedResponse()
                every { invoiceRepository.invoiceAction(any()) } returns Single.just(mockResponse)

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.invoiceActionResponseMsgLiveData.value shouldBe "Invoice accepted successfully"
            }

            test("handles null message in response") {
                loadMockInvoice()
                val mockResponse = InvoiceTestDataFactory.createInvoiceActionResponse(message = null)
                every { invoiceRepository.invoiceAction(any()) } returns Single.just(mockResponse)

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.invoiceActionResponseMsgLiveData.value.shouldBeEmpty()
            }
        }

        context("input sanitization") {
            test("trims whitespace from invoice number") {
                loadMockInvoice()
                val requestSlot = slot<InvoiceActionRequest>()
                every { invoiceRepository.invoiceAction(capture(requestSlot)) } returns 
                    Single.just(InvoiceTestDataFactory.createAcceptedResponse())

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "  INV123  ", "2024-01-01")

                requestSlot.captured.vendorInvoiceNumber shouldBe "INV123"
            }

            test("truncates invoice number exceeding max length") {
                loadMockInvoice()
                val requestSlot = slot<InvoiceActionRequest>()
                every { invoiceRepository.invoiceAction(capture(requestSlot)) } returns 
                    Single.just(InvoiceTestDataFactory.createAcceptedResponse())

                val longInvoiceNumber = "A".repeat(20)
                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, longInvoiceNumber, "2024-01-01")

                requestSlot.captured.vendorInvoiceNumber shouldBe "A".repeat(16)
            }

            test("trims whitespace from invoice date") {
                loadMockInvoice()
                val requestSlot = slot<InvoiceActionRequest>()
                every { invoiceRepository.invoiceAction(capture(requestSlot)) } returns 
                    Single.just(InvoiceTestDataFactory.createAcceptedResponse())

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "  2024-01-01  ")

                requestSlot.captured.vendorInvoiceDate shouldBe "2024-01-01"
            }
        }

        context("request payload verification") {
            test("accept sends correct request to repository") {
                loadMockInvoice()
                val requestSlot = slot<InvoiceActionRequest>()
                every { invoiceRepository.invoiceAction(capture(requestSlot)) } returns 
                    Single.just(InvoiceTestDataFactory.createAcceptedResponse())

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                requestSlot.captured.apply {
                    ticketId shouldBe "TICKET123"
                    action shouldBe "accept"
                    vendorInvoiceNumber shouldBe "INV123"
                    vendorInvoiceDate shouldBe "2024-01-01"
                    rejectionRemark.shouldBeNull()
                }
            }

            test("reject sends correct request with rejection remark") {
                loadMockInvoice()
                val requestSlot = slot<InvoiceActionRequest>()
                every { invoiceRepository.invoiceAction(capture(requestSlot)) } returns 
                    Single.just(InvoiceTestDataFactory.createRejectedResponse())

                viewModel.acceptRejectInvoice(ConfirmationType.REJECT, null, null)

                requestSlot.captured.apply {
                    ticketId shouldBe "TICKET123"
                    action shouldBe "reject"
                    vendorInvoiceNumber.shouldBeNull()
                    vendorInvoiceDate.shouldBeNull()
                    rejectionRemark shouldBe "Rejected by Vendor"
                }
            }
        }

        context("loading state") {
            test("sets loading true then false on success") {
                loadMockInvoice()
                every { invoiceRepository.invoiceAction(any()) } returns 
                    Single.just(InvoiceTestDataFactory.createAcceptedResponse())
                
                // Capture AFTER loadMockInvoice to only get states from acceptRejectInvoice
                val loadingStates = viewModel.isLoadingLiveData.captureValues()

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                loadingStates shouldContainExactly listOf(true, false)
            }

            test("sets loading false on error") {
                loadMockInvoice()
                val error = RuntimeException("API error")
                every { invoiceRepository.invoiceAction(any()) } returns Single.error(error)

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.isLoadingLiveData.value shouldBe false
            }
        }

        context("error handling") {
            test("error posts to exceptionLiveData") {
                loadMockInvoice()
                val error = RuntimeException("API error")
                every { invoiceRepository.invoiceAction(any()) } returns Single.error(error)

                viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

                viewModel.exceptionLiveData.value shouldBe error
                viewModel.isLoadingLiveData.value shouldBe false
            }
        }
    }

    // ==================== validateInputs Tests ====================

    context("validateInputs") {
        test("valid inputs for accept returns true") {
            val result = viewModel.validateInputs("INV123", isRejectCase = false)
            result shouldBe true
        }

        test("empty invoice for reject returns true") {
            val result = viewModel.validateInputs("", isRejectCase = true)
            result shouldBe true
        }

        test("empty invoice for accept returns false") {
            val result = viewModel.validateInputs("", isRejectCase = false)
            result shouldBe false
        }
    }

    // ==================== ConfirmationType Tests ====================

    context("ConfirmationType") {
        test("ACCEPT has correct value") {
            ConfirmationType.ACCEPT.value shouldBe "accept"
        }

        test("REJECT has correct value") {
            ConfirmationType.REJECT.value shouldBe "reject"
        }
    }

    // ==================== InvoiceReviewErrorType Tests ====================

    context("InvoiceReviewErrorType") {
        test("all error types are defined") {
            val errorTypes = InvoiceReviewErrorType.values()
            errorTypes shouldHaveSize 9
            errorTypes shouldContain InvoiceReviewErrorType.TRANSACTION_ID_REQUIRED
            errorTypes shouldContain InvoiceReviewErrorType.INVOICE_NUMBER_REQUIRED
            errorTypes shouldContain InvoiceReviewErrorType.INVOICE_NUMBER_INVALID_CHARS
            errorTypes shouldContain InvoiceReviewErrorType.INVOICE_NUMBER_MAX_LENGTH
            errorTypes shouldContain InvoiceReviewErrorType.INVOICE_DETAILS_NOT_LOADED
            errorTypes shouldContain InvoiceReviewErrorType.INVOICE_TICKET_ID_MISSING
            errorTypes shouldContain InvoiceReviewErrorType.ENTER_INVOICE_NUMBER
            errorTypes shouldContain InvoiceReviewErrorType.ENTER_INVOICE_DATE
            errorTypes shouldContain InvoiceReviewErrorType.ALREADY_PROCESSED
        }
    }

    // ==================== Repository Interaction Verification ====================

    context("repository interactions") {
        test("fetchInvoiceDetails calls repository with correct fmsTicketId") {
            viewModel.fmsTicketId = "TXN456"
            every { invoiceRepository.getInvoiceDetails("TXN456") } returns 
                Single.just(InvoiceTestDataFactory.createInvoiceDetails())

            viewModel.fetchInvoiceDetails()

            verify(exactly = 1) { invoiceRepository.getInvoiceDetails("TXN456") }
        }

        test("acceptRejectInvoice calls repository exactly once") {
            loadMockInvoice()
            every { invoiceRepository.invoiceAction(any()) } returns 
                Single.just(InvoiceTestDataFactory.createAcceptedResponse())

            viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, "INV123", "2024-01-01")

            verify(exactly = 1) { invoiceRepository.invoiceAction(any()) }
        }

        test("validation failure does not call repository") {
            loadMockInvoice()

            viewModel.acceptRejectInvoice(ConfirmationType.ACCEPT, null, "2024-01-01")

            verify(exactly = 0) { invoiceRepository.invoiceAction(any()) }
        }
    }
})

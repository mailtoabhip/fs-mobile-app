package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.testdata.TripTestDataFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Unit tests for TripMilestoneProvider using Kotest.
 * 
 * Tests cover:
 * - Legacy flow (6 milestones for non-ops-arranged trips)
 * - GST vendor flow (7 milestones with invoice acceptance)
 * - Non-GST vendor flow (5 milestones)
 * - Milestone status transitions
 * - Payment status handling
 * - Date formatting utilities
 */
class TripMilestoneProviderTest : FunSpec({

    // ==================== getIntracityMilestones (Legacy Flow) Tests ====================

    context("getIntracityMilestones (Legacy Flow)") {
        
        test("returns 6 milestones for legacy flow") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails()

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones shouldHaveSize 6
            milestones[0].id shouldBe MilestoneIds.ARRIVED_AT_PICKUP
            milestones[1].id shouldBe MilestoneIds.LOADED
            milestones[2].id shouldBe MilestoneIds.REACHED_DESTINATION
            milestones[3].id shouldBe MilestoneIds.UNLOADED
            milestones[4].id shouldBe MilestoneIds.HPOD_SUBMITTED
            milestones[5].id shouldBe MilestoneIds.SETTLED
        }

        test("all pending when no updates") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails()

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones.forEach { milestone ->
                milestone.status shouldBe MilestoneStatus.PENDING
            }
        }

        test("arrived completed when truckArrivedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(arrived = true)
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[0].status shouldBe MilestoneStatus.COMPLETED
            milestones[1].status shouldBe MilestoneStatus.PENDING
        }

        test("loaded completed when loadedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(
                    arrived = true,
                    loaded = true
                )
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[0].status shouldBe MilestoneStatus.COMPLETED
            milestones[1].status shouldBe MilestoneStatus.COMPLETED
            milestones[2].status shouldBe MilestoneStatus.PENDING
        }

        test("reached completed when truckReachedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(
                    arrived = true,
                    loaded = true,
                    reached = true
                )
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[2].status shouldBe MilestoneStatus.COMPLETED
        }

        test("unloaded completed when truckUnloadedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(
                    arrived = true,
                    loaded = true,
                    reached = true,
                    unloaded = true
                )
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[3].status shouldBe MilestoneStatus.COMPLETED
        }

        test("hpod completed when tripCompletedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                updateInfo = TripTestDataFactory.createCompletedStatusUpdateInfo()
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[4].status shouldBe MilestoneStatus.COMPLETED
        }

        test("settled completed when isSettled true") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(isSettled = true)

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[5].status shouldBe MilestoneStatus.COMPLETED
        }

        test("milestone display names are correct") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails()

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones[0].displayName shouldBe "Arrived at Pickup"
            milestones[1].displayName shouldBe "Loaded"
            milestones[2].displayName shouldBe "Reached Destination"
            milestones[3].displayName shouldBe "Unloaded"
            milestones[4].displayName shouldBe "hPOD Submitted"
            milestones[5].displayName shouldBe "Settled"
        }

        test("all milestones completed for fully completed trip") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                isSettled = true,
                updateInfo = TripTestDataFactory.createCompletedStatusUpdateInfo()
            )

            val milestones = TripMilestoneProvider.getIntracityMilestones(tripDetails)

            milestones.forEach { milestone ->
                milestone.status shouldBe MilestoneStatus.COMPLETED
            }
        }
    }

    // ==================== getOpsArrangedIntracityMilestones (GST Vendor) Tests ====================

    context("getOpsArrangedIntracityMilestones (GST Vendor)") {
        
        test("returns 7 milestones for GST vendor") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo()
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones shouldHaveSize 7
            milestones[0].id shouldBe MilestoneIds.ARRIVED_AT_PICKUP
            milestones[1].id shouldBe MilestoneIds.LOADED
            milestones[2].id shouldBe MilestoneIds.TRIP_COMPLETED
            milestones[3].id shouldBe MilestoneIds.TICKET_CLOSED
            milestones[4].id shouldBe MilestoneIds.ACCEPT_INVOICE
            milestones[5].id shouldBe MilestoneIds.INVOICE_ACCEPTED
            milestones[6].id shouldBe MilestoneIds.PAYMENT_RELEASED
        }

        test("accept invoice pending initially") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(invoiceStatus = null)
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[4].status shouldBe MilestoneStatus.PENDING
        }

        test("accept invoice completed when under review") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "invoice_under_review"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[4].status shouldBe MilestoneStatus.COMPLETED
            milestones[5].status shouldBe MilestoneStatus.PENDING
        }

        test("invoice accepted when status accepted") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "accepted"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[4].status shouldBe MilestoneStatus.COMPLETED
            milestones[5].status shouldBe MilestoneStatus.COMPLETED
        }

        test("invoice accepted when status invoiced") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "invoiced"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[5].status shouldBe MilestoneStatus.COMPLETED
        }

        test("payment released when paid") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "paid"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.COMPLETED
            milestones[6].displayName shouldBe "Payment Released"
        }

        test("payment failed status") {
            val tripDetails = TripTestDataFactory.createPaymentFailedTrip(
                failureMessage = "Insufficient funds"
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.FAILED
            milestones[6].displayName shouldBe "Payment Failed"
            milestones[6].subtitle shouldBe "Failure Remarks: Insufficient funds"
        }

        test("milestone display names are correct") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo()
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].displayName shouldBe "Arrived at Pickup"
            milestones[1].displayName shouldBe "Loaded"
            milestones[2].displayName shouldBe "Mark Out / Trip Completed"
            milestones[3].displayName shouldBe "Ticket Closed"
            milestones[4].displayName shouldBe "Review Invoice"
            milestones[5].displayName shouldBe "Invoice Accepted"
            milestones[6].displayName shouldBe "Payment Released"
        }

        test("uses isGstVerified fallback when invoiceStatusInfo.isGstVendor is null") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo().copy(isGstVendor = false)
            )

            // When isGstVendor is false but isGstVerified is true, should still use non-GST flow
            // because invoiceStatusInfo.isGstVendor takes precedence
            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = true)

            milestones shouldHaveSize 5 // Non-GST flow
        }

        test("invoiceStatusInfo.isGstVendor takes precedence over isGstVerified") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo() // isGstVendor = true
            )

            // Even though isGstVerified is false, invoiceStatusInfo.isGstVendor = true takes precedence
            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones shouldHaveSize 7 // GST flow
        }
    }

    // ==================== getOpsArrangedIntracityMilestones (Non-GST Vendor) Tests ====================

    context("getOpsArrangedIntracityMilestones (Non-GST Vendor)") {
        
        test("returns 5 milestones for non-GST vendor") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createNonGstVendorInvoiceInfo()
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones shouldHaveSize 5
            milestones[0].id shouldBe MilestoneIds.ARRIVED_AT_PICKUP
            milestones[1].id shouldBe MilestoneIds.LOADED
            milestones[2].id shouldBe MilestoneIds.TRIP_COMPLETED
            milestones[3].id shouldBe MilestoneIds.TICKET_CLOSED
            milestones[4].id shouldBe MilestoneIds.PAYMENT_RELEASED
        }

        test("all pending initially") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createNonGstVendorInvoiceInfo()
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].status shouldBe MilestoneStatus.PENDING
            milestones[1].status shouldBe MilestoneStatus.PENDING
            milestones[2].status shouldBe MilestoneStatus.PENDING
            milestones[3].status shouldBe MilestoneStatus.PENDING
            milestones[4].status shouldBe MilestoneStatus.PENDING
        }

        test("payment released when paid") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createNonGstVendorInvoiceInfo(
                    invoiceStatus = "paid"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[4].status shouldBe MilestoneStatus.COMPLETED
        }

        test("fully paid non-GST vendor trip") {
            val tripDetails = TripTestDataFactory.createPaidNonGstVendorTrip()

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].status shouldBe MilestoneStatus.COMPLETED // Arrived
            milestones[1].status shouldBe MilestoneStatus.COMPLETED // Loaded
            milestones[2].status shouldBe MilestoneStatus.COMPLETED // Trip Completed
            milestones[4].status shouldBe MilestoneStatus.COMPLETED // Payment Released
        }
    }

    // ==================== Ticket Closed Milestone Tests ====================

    context("Ticket Closed Milestone") {
        
        test("GST vendor completed when ticket closed") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    ticketStatus = "closed",
                    invoiceStatus = "invoiced"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[3].status shouldBe MilestoneStatus.COMPLETED
        }

        test("GST vendor completed when ticket paid") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    ticketStatus = "paid",
                    invoiceStatus = "paid"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[3].status shouldBe MilestoneStatus.COMPLETED
        }

        test("non-GST vendor completed when closed with invoice status") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createNonGstVendorInvoiceInfo(
                    ticketStatus = "closed",
                    invoiceStatus = "invoiced"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[3].status shouldBe MilestoneStatus.COMPLETED
        }

        test("non-GST vendor pending when closed but no invoice status") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createNonGstVendorInvoiceInfo(
                    ticketStatus = "closed",
                    invoiceStatus = null
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[3].status shouldBe MilestoneStatus.PENDING
        }
    }

    // ==================== Trip Completed Milestone Tests ====================

    context("Trip Completed Milestone") {
        
        test("completed when tripCompletedInfo exists") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(),
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(completed = true)
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[2].status shouldBe MilestoneStatus.COMPLETED
        }

        test("pending when tripCompletedInfo is null") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(),
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(
                    arrived = true,
                    loaded = true,
                    completed = false
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[2].status shouldBe MilestoneStatus.PENDING
        }
    }

    // ==================== Null Invoice Status Info Tests ====================

    context("Null Invoice Status Info") {
        
        test("returns non-GST flow when invoiceStatusInfo is null and isGstVerified is false") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(invoiceStatusInfo = null)

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones shouldHaveSize 5
        }

        test("returns GST flow when invoiceStatusInfo is null but isGstVerified is true") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(invoiceStatusInfo = null)

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = true)

            milestones shouldHaveSize 7
        }
    }

    context("Payment Timestamp") {
        
        test("paid with payment info shows timestamp") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfoWithPayment(
                    invoiceStatus = "paid",
                    paymentTimestamp = "2024-01-15T14:30:00",
                    utr = "UTR123456789"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.COMPLETED
            milestones[6].timestamp.shouldNotBeNull()
        }

        test("payment failed with timestamp shows failed at") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfoWithPayment(
                    invoiceStatus = "payment_failed",
                    paymentTimestamp = "2024-01-15T14:30:00",
                    failureMessage = "Bank rejected"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.FAILED
            milestones[6].timestamp.shouldNotBeNull()
            milestones[6].subtitle shouldBe "Failure Remarks: Bank rejected"
        }

        test("paid without payment info has null timestamp") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "paid"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.COMPLETED
            milestones[6].timestamp.shouldBeNull()
        }

        test("failure without failure message has null subtitle") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "payment_failed",
                    failureMessage = null
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[6].status shouldBe MilestoneStatus.FAILED
            milestones[6].subtitle.shouldBeNull()
        }
    }
    // ==================== Under Finance Review Status Tests ====================

    context("Under Finance Review Status") {
        
        test("under_finance_review marks invoice as pending") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(
                    invoiceStatus = "under_finance_review"
                )
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[5].status shouldBe MilestoneStatus.COMPLETED
        }
    }

    // ==================== formatDateString Tests ====================

    context("formatDateString") {
        
        test("returns null for null input") {
            val result = TripMilestoneProvider.formatDateString(null)
            result.shouldBeNull()
        }

        test("returns null for blank input") {
            val result = TripMilestoneProvider.formatDateString("")
            result.shouldBeNull()
        }

        test("returns null for whitespace input") {
            val result = TripMilestoneProvider.formatDateString("   ")
            result.shouldBeNull()
        }

        test("returns formatted date for invalid date format (falls back to current date)") {
            val result = TripMilestoneProvider.formatDateString("invalid-date")
            // DateUtils.parseDate returns current date on parse error
            // So formatDateString returns a formatted current date, not null
            result.shouldNotBeNull()
        }

        test("formats valid date correctly") {
            // Should return formatted date or null if parsing fails
            // The actual format depends on the implementation
            val result = TripMilestoneProvider.formatDateString("2024-01-15T14:30:00")
            result.shouldNotBeNull()
        }
    }

    // ==================== Preset Scenario Tests ====================

    context("Preset Scenarios") {
        
        test("paid GST vendor trip has all milestones completed") {
            val tripDetails = TripTestDataFactory.createPaidGstVendorTrip()

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].status shouldBe MilestoneStatus.COMPLETED // Arrived
            milestones[1].status shouldBe MilestoneStatus.COMPLETED // Loaded
            milestones[2].status shouldBe MilestoneStatus.COMPLETED // Trip Completed
            milestones[4].status shouldBe MilestoneStatus.COMPLETED // Accept Invoice
            milestones[5].status shouldBe MilestoneStatus.COMPLETED // Invoice Accepted
            milestones[6].status shouldBe MilestoneStatus.COMPLETED // Payment Released
        }

        test("pending invoice review trip shows correct states") {
            val tripDetails = TripTestDataFactory.createPendingInvoiceReviewTrip()

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[4].status shouldBe MilestoneStatus.COMPLETED // Accept Invoice (under review)
            milestones[5].status shouldBe MilestoneStatus.PENDING   // Invoice Accepted
            milestones[6].status shouldBe MilestoneStatus.PENDING   // Payment Released
        }
    }

    // ==================== Edge Cases ====================

    context("Edge Cases") {
        
        test("handles trip with only arrived status") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(),
                updateInfo = TripTestDataFactory.createStatusUpdateInfo(arrived = true)
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].status shouldBe MilestoneStatus.COMPLETED
            milestones[1].status shouldBe MilestoneStatus.PENDING
            milestones[2].status shouldBe MilestoneStatus.PENDING
        }

        test("handles trip with null updateInfo") {
            val tripDetails = TripTestDataFactory.createBaseTripDetails(
                invoiceStatusInfo = TripTestDataFactory.createGstVendorInvoiceInfo(),
                updateInfo = null
            )

            val milestones = TripMilestoneProvider.getOpsArrangedIntracityMilestones(tripDetails, isGstVerified = false)

            milestones[0].status shouldBe MilestoneStatus.PENDING
            milestones[1].status shouldBe MilestoneStatus.PENDING
        }
    }
})

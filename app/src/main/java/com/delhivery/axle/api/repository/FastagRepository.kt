package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.FastagLeadRequest
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.request.IssueTagRequest
import com.delhivery.axle.api.response.BarcodeLookupResponse
import com.delhivery.axle.api.response.FormConfigResponse
import com.delhivery.axle.api.response.toResource
import com.delhivery.axle.api.service.FastagService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Repository for all FASTag-related operations.
 * Provides methods for truck listing, balance, transactions, recharge, and disputes.
 */
class FastagRepository @Inject constructor(
    private val fastagService: FastagService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

    /**
     * Get list of FASTag-linked vehicles.
     * @param limit Number of items per page
     * @param offset Pagination offset
     */
    fun getFastagListing(limit: Int? = null, offset: Int? = null) =
        fastagService.getFastagListing(limit, offset).convertResponse()

    /**
     * Get FASTag balance for a specific tag.
     */
    fun getFastagBalance(tagId: String) =
        fastagService.getFastagBalance(tagId).convertResponse()

    /**
     * Get FASTag transactions listing.
     */
    fun getFastagTransactions(tagId: String, limit: Int, offset: Int) =
        fastagService.getFastagTransactions(tagId, offset, limit).convertResponse()

    /**
     * Download FASTag transactions.
     */
    fun downloadFastagTransactions(tagId: String, fromDate: String?, toDate: String?) =
        fastagService.downloadFastagTransactions(tagId, fromDate, toDate)

    /**
     * Submit FASTag lead request (buy FASTag).
     */
    fun submitFastagLead(fastagLeadRequest: FastagLeadRequest) =
        fastagService.submitFastagLead(fastagLeadRequest).convertResponse()

    /**
     * Recharge FASTag from wallet.
     */
    fun rechargeFastag(fastagRechargeRequest: FastagRechargeRequest) =
        fastagService.rechargeFastag(fastagRechargeRequest).convertResponse()

    /**
     * Fetch FASTag status.
     */
    fun fetchFastagStatus(tagId: String) =
        fastagService.fetchFastagStatus(tagId).convertResponse()

    /**
     * Get FASTag transactions by toll plaza.
     */
    fun getFastagTransactionsByTollPlaza(
        tollPlazaId: String,
        dateTime: String? = null,
        fastagId: String? = null,
        limit: Int? = 50,
        offset: Int? = 0,
        txnId: String? = null
    ) = fastagService.getFastagTransactionsByTollPlaza(tollPlazaId, dateTime, limit, offset, fastagId, txnId).convertResponse()

    /**
     * Get transaction dispute details.
     */
    fun getDisputeIssues(txnId: String) =
        fastagService.getTransactionDispute(txnId).convertResponse()

    /**
     * Get dispute issues list.
     */
    fun getDisputeIssuesList(partner: String) =
        fastagService.getDisputeIssuesList(partner).convertResponse()

    /**
     * Get dispute form configuration.
     */
    fun getDisputeFormConfig(disputeTypeCode: String) =
        fastagService.getDisputeFormConfig(disputeTypeCode).convertResponse()
            .map { fields -> FormConfigResponse(fields) }

    /**
     * Lookup barcode from dispatch table.
     */
    suspend fun barcodeLookup(orderId: String, orderItemId: Int, vehicleClass: String): Resource<BarcodeLookupResponse> =
        safeApiCall {
            fastagService.barcodeLookup(orderId, orderItemId, vehicleClass).toResource()
        }

    /**
     * Search products and barcodes from IDFC.
     */
    suspend fun searchProductBarcode(journeyId: String, barcodeLast4: String): Resource<com.delhivery.axle.api.response.ProductBarcodeResponse> =
        safeApiCall {
            fastagService.searchProductBarcode(
                com.delhivery.axle.api.request.ProductBarcodeRequest(journeyId, barcodeLast4)
            ).toResource()
        }

    /**
     * Generate consent OTP for tag mapping.
     */
    suspend fun generateOtp(journeyId: String, barcode: String, tagId: String): Resource<Any> =
        safeApiCall {
            fastagService.generateOtp(
                com.delhivery.axle.api.request.GenerateOtpRequest(journeyId, barcode, tagId)
            ).toResource()
        }

    /**
     * Issue FASTag and process payment.
     */
    suspend fun issueTag(
        journeyId: String,
        orderId: String,
        orderItemId: Int,
        barcode: String,
        otp: String
    ): Resource<com.delhivery.axle.api.response.IssueTagResponse> =
        safeApiCall {
            fastagService.issueTag(
                IssueTagRequest(journeyId, orderId, orderItemId, barcode, otp)
            ).toResource()
        }

    /**
     * Submit dispute with multipart form data.
     */
    fun submitDispute(
        txnId: MultipartBody.Part? = null,
        tollPlazaId: MultipartBody.Part? = null,
        refundAmount: MultipartBody.Part? = null,
        comment: MultipartBody.Part? = null,
        raisedAgainst: MultipartBody.Part? = null,
        additionalTxnId: MultipartBody.Part? = null,
        uploadDoc1: MultipartBody.Part? = null,
        uploadDoc2: MultipartBody.Part? = null,
        uploadDoc3: MultipartBody.Part? = null
    ) = fastagService.submitDispute(
        txnId, tollPlazaId, refundAmount, comment, raisedAgainst,
        additionalTxnId, uploadDoc1, uploadDoc2, uploadDoc3
    ).convertResponse()
}

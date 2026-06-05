package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.FastagLeadRequest
import com.delhivery.axle.api.request.FastagRechargeRequest
import com.delhivery.axle.api.request.IssueTagRequest
import com.delhivery.axle.api.response.BarcodeLookupResponse
import com.delhivery.axle.api.response.FastagImageUploadResponse
import com.delhivery.axle.api.response.FastagImageValidateResponse
import com.delhivery.axle.api.response.FormConfigResponse
import com.delhivery.axle.api.response.OrderItem
import com.delhivery.axle.api.response.RcProcessResponse
import com.delhivery.axle.api.response.RcProcessStatusResponse
import com.delhivery.axle.api.response.VehicleImageProcessResponse
import com.delhivery.axle.api.response.VehicleImageProcessStatusResponse
import com.delhivery.axle.api.response.toResource
import com.delhivery.axle.api.service.FasTAGIssuanceService
import com.delhivery.axle.api.service.FasTAGKycService
import com.delhivery.axle.api.service.FastagService
import com.delhivery.axle.injection.qualifier.IoDispatcher
import com.delhivery.axle.ui.fastag.tagAssignment.pendingActions.PendingActionsResponse
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertResponse
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Repository for all FASTag-related operations.
 * Provides methods for truck listing, balance, transactions, recharge, and disputes.
 */
class FastagRepository @Inject constructor(
    private val fastagService: FastagService,
    private val kycService: FasTAGKycService,
    errorLogger: ErrorLogger,
    private val fasTAGIssuanceService: FasTAGIssuanceService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
            kycService.barcodeLookup(orderId, orderItemId, vehicleClass).toResource()
        }

    /**
     * Search products and barcodes from IDFC.
     */
    suspend fun searchProductBarcode(journeyId: String, barcode: String): Resource<com.delhivery.axle.api.response.ProductBarcodeResponse> =
        safeApiCall {
            kycService.searchProductBarcode(
                com.delhivery.axle.api.request.ProductBarcodeRequest(journeyId, barcode)
            ).toResource()
        }

    /**
     * Generate consent OTP for tag mapping.
     */
    suspend fun generateOtp(journeyId: String, barcode: String, tagId: String): Resource<Any> =
        safeApiCall {
            kycService.generateOtp(
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
            fasTAGIssuanceService.issueTag(
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

    // ---- Coroutine-based methods (Tag Issuance flow) ----

    suspend fun getOrderItems(orderId: String): Resource<List<OrderItem>> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.getOrderItems(orderId)
                response.toResource()
            }
        }

    suspend fun uploadRcImages(
        rcFront: MultipartBody.Part,
        rcBack: MultipartBody.Part,
        orderId: MultipartBody.Part,
        orderItemId: MultipartBody.Part
    ): Resource<RcProcessResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.uploadRcImages(rcFront, rcBack, orderId, orderItemId)
                response.toResource()
            }
        }

    suspend fun getRcProcessStatus(jobId: String): Resource<RcProcessStatusResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.getRcProcessStatus(jobId)
                response.toResource()
            }
        }

    suspend fun uploadVehicleImages(
        vehicleFront: MultipartBody.Part,
        vehicleSide: MultipartBody.Part,
        orderId: MultipartBody.Part,
        orderItemId: MultipartBody.Part,
        journeyId: MultipartBody.Part
    ): Resource<VehicleImageProcessResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.uploadVehicleImages(vehicleFront, vehicleSide, orderId, orderItemId, journeyId)
                response.toResource()
            }
        }

    suspend fun getVehicleImageProcessStatus(jobId: String): Resource<VehicleImageProcessStatusResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.getVehicleImageProcessStatus(jobId)
                response.toResource()
            }
        }

    suspend fun uploadFastagImage(
        fastagImage: MultipartBody.Part,
        journeyId: MultipartBody.Part,
        orderId: MultipartBody.Part,
        orderItemId: MultipartBody.Part
    ): Resource<FastagImageUploadResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.uploadFastagImage(fastagImage, journeyId, orderId, orderItemId)
                response.toResource()
            }
        }

    suspend fun validateFastagImage(journeyId: String): Resource<FastagImageValidateResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val request = com.delhivery.axle.api.request.FastagImageValidateRequest(journeyId = journeyId)
                val response = kycService.validateFastagImage(request)
                response.toResource()
            }
        }

    // ---- RxJava-based: Recharge status ----

    fun fetchRechargeStatus(userId: String, rechargeId: String) =
        fastagService.fetchRechargeStatus(
            userId = userId,
            request = JsonObject().apply { addProperty("recharge_id", rechargeId) }
        ).convertResponse()

    /**
     * Get pending actions for FASTag tag issuance.
     * GET /fastag/tag-issuance/v1/pending-actions
     */
    suspend fun getPendingActions(): Resource<PendingActionsResponse> = safeApiCall {
        fastagService.getPendingActions().toResource()
    }
}

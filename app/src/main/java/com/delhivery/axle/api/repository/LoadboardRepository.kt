package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.request.GstDetailRequest
import com.delhivery.axle.api.request.GstNumberRequest
import com.delhivery.axle.api.request.PanVerificationRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.DisputeIssuesResponse
import com.delhivery.axle.api.response.DisputeSubmissionResponse
import com.delhivery.axle.api.response.DisputeType
import com.delhivery.axle.api.response.FastagTransactionByTollPlaza
import com.delhivery.axle.api.response.FastagTransactionsByTollPlazaResponse
import com.delhivery.axle.api.response.FormConfigResponse
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.google.gson.JsonObject
import javax.inject.Inject
import okhttp3.MultipartBody

class LoadboardRepository @Inject constructor(
    private val loadboardService: LoadBoardService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {

    /**
     * gst numbers
     */
    fun gstNumbers(pan_number: String)= loadboardService.getGstNumbers(GstNumberRequest(pan_number)).convertResponse()

    /**
     * gst details
     */
    fun gstDetails(gst_number: String)= loadboardService.getGstDetails(GstDetailRequest(gst_number)).convertResponse()


    fun addAddress(phone_number: String?,address:String?,proof_document_type:String?,document_url:List<String>?,address_type:String?,isDeleted:Boolean) = loadboardService.addAddress(
        AddAddressModel(phone_number,address,proof_document_type,document_url,address_type, isDeleted)
    ).convertMessageResponse()

    fun updateCommunicationAddress(businessAddress:String,isSameAsGst:Boolean,phoneNumber: String) = loadboardService.updateNewAddress(
        UpdateAddressVerificationRequest(phoneNumber,businessAddress,isSameAsGst)
    ).convertMessageResponse()


    /**
     * update user
     */
    fun updateUser(updateUserRequest: UpdateUserRequest) =
           loadboardService.updateUser(updateUserRequest).convertMessageResponse()

    /**
     * reset kyc
     */
    fun resetKyc(resetKycDataRequest: ResetKycDataRequest) =
        loadboardService.resetKyc(resetKycDataRequest).convertMessageResponse()

    /**
     * add route
     */
    fun addRoute(updateUserRequest: UpdateUserRequest) =
        loadboardService.updateUser(updateUserRequest).convertMessageResponse()

    /**
     * validate RC
     */
    fun validateRC(rcNumber :String)  =
        loadboardService.validateRC(RcVerificationRequest(rcNumber)).convertResponse()

    /**
     * upload Business Verification Document
     */
    fun uploadVerificationDoc(verificationDocUploadRequest: VerificationDocUploadRequest)  =
        loadboardService.uploadDocument(verificationDocUploadRequest).convertMessageResponse()


    /**
     * get Aadhaar or GST
     */
    fun getGstOrAadhaarOtp(verificationType:String,verificationId:String)= loadboardService.getGstOrAadhaarOtp(
        GstOrAadhaarOtpGetRequest(verificationType,verificationId)
    ).convertMessageResponse()

    /**
     * verify aadhaar otp
     */
    fun verifyGstOrAadhaarOtp(verificationType:String,verificationId:String,otp:String)= loadboardService.verifyGstOrAadhaarOtp(
        GstOrAadhaarOtpVerifyRequest(verificationType,verificationId,otp)
    ).convertMessageResponse()


    /**
     * verify by ocr
     */
    fun verifyByDocUpload(verificationType:String,verificationId:String,docList:List<String>)= loadboardService.verifyByDocUpload(
        GstOrAadhaarDocRequest(verificationType,verificationId,docList)
    ).convertResponse()

    fun createUser(updateUserRequest: UpdateUserRequest)
     =  loadboardService.updateUser(updateUserRequest)
            .map {
                Pair(true, "Account created")
            }
            .onErrorReturn {
                /* handle error if needed */
                    Pair(false, it.errorResponseBody()?.errorBody?.errorMessage.toString())
            }
    fun validatePanNumber(panNumber:String)= loadboardService.validatePanNumber(PanVerificationRequest(panNumber)).convertResponse()

    /**
     * Get team members
     */
    fun getUserTeamMembers(uuid:String) = loadboardService.getTeamMembers(uuid).convertResponse()

    /**
     * Create secondary user
     */
    fun createSecondaryUser(jsonObject: JsonObject) =
            loadboardService.createSecondaryUser(jsonObject).convertMessageResponse()

    /**
     * Update secondary user
     */
    fun updateSecondaryUser(jsonObject: JsonObject) =
            loadboardService.updateSecondaryUser(jsonObject).convertMessageResponse()

    /**
     * Update Admin user
     */
    fun updateAdminUser(jsonObject: JsonObject) =
            loadboardService.updateAdminUser(jsonObject).convertMessageResponse()

    /**
     * Get KYC details
     */
    fun getKycDetails(uuid:String) = loadboardService.getKYCDetails(uuid).convertResponse()

    /**
     * Add route details
     */
    fun addRouteDetails(uuid:String,updateRouteRequest: UpdateRouteRequest) = loadboardService.addRoute(uuid,updateRouteRequest).convertMessageResponse()

    /**
     * Edit route details
     */
    fun editRouteDetails(uuid:String,updateRouteRequest: UpdateRouteRequest) = loadboardService.editRoute(uuid,updateRouteRequest).convertMessageResponse()

    /**
     * Delete route details
     */
    fun deleteRouteDetails(uuid:String,updateRouteRequest: UpdateRouteRequest) = loadboardService.deleteRoute(uuid,updateRouteRequest).convertMessageResponse()

    /**
     *get popular locations
     */
    fun getPopularLocations(uuid:String) = loadboardService.getPopularLocations(uuid).convertResponse()

    fun getBankName(bankValidationRequest: BankValidationRequest) = loadboardService.getBankName(bankValidationRequest = bankValidationRequest).convertResponse()

    /**
     * Get FASTag balance
     */
    fun getFastagBalance(tagId: String) = loadboardService.getFastagBalance(tagId).convertResponse()

    fun getInventories(request: JsonObject) = loadboardService.getInventories(request)
        .map { response ->
            if (response.success) {
                response
            } else {
                throw Exception("API returned success: false")
            }
        }


    /**
     * Get transaction dispute details for FASTag
     */
    fun getDisputeIssues(txnId: String) = loadboardService.getTransactionDispute(txnId).convertResponse()

    /**
     * Get dispute issues list for FASTag
     */
    fun getDisputeIssuesList(partner: String) = loadboardService.getDisputeIssuesList(partner).convertResponse()


    fun downloadFastagTransactions(tagId: String, from_date: String?,to_date: String?) = loadboardService.downloadFastagTransactions(tagId, from_date, to_date)

    fun getFastagTransactions(tagId: String, limit: Int, offset: Int) = loadboardService.getFastagTransactions(tagId, offset, limit).convertResponse()

    /**
     * Submit FASTag lead request
     */
    fun submitFastagLead(fastagLeadRequest: FastagLeadRequest) = loadboardService.submitFastagLead(fastagLeadRequest).convertResponse()

    /**
     * Recharge FASTag from wallet
     */
    fun rechargeFastag(fastagRechargeRequest: FastagRechargeRequest) = loadboardService.rechargeFastag(fastagRechargeRequest).convertResponse()

    /**
     * Fetch FASTag status
     */
    fun fetchFastagStatus(tagId: String) = loadboardService.fetchFastagStatus(tagId).convertResponse()

    /**
     * Fetch wallet details
     */
    fun fetchWalletDetails() =
        loadboardService.fetchWalletDetails().convertResponse()

    fun createWallet() =
        loadboardService.createWallet().convertResponse()

    fun fetchWalletTransactionList(start: String, end: String, walletId: String, limit: Int = 10, offset: Int = 0) =
        loadboardService.fetchWalletTransactionList(start, end, walletId, limit, offset).convertResponse()

    fun fetchWalletTransactionList(start: String, end: String, walletId: String, limit: Int = 10, offset: Int = 0, type: String? = null) =
        loadboardService.fetchWalletTransactionList(start, end, walletId, limit, offset, type).convertResponse()

    fun fetchTransactionStatus(start: String, txnId: String) =
        loadboardService.fetchTransactionStatus(start, txnId).convertResponse()

    fun fetchWalletRechargeList(walletId: String, start: String, end: String, limit: Int = 10, offset: Int = 0) =
        loadboardService.fetchWalletRechargeList(walletId, start, end, limit, offset).convertResponse()

    fun fetchRechargeStatus(rechargeId: String, start: String) =
        loadboardService.fetchRechargeStatus(start, rechargeId).convertResponse()


    /**
     * Get FASTag transactions by toll plaza or fastag ID
     */
    fun getFastagTransactionsByTollPlaza(
        tollPlazaId: String,
        dateTime: String? = null,
        fastagId: String? = null,
        limit: Int? = 50,
        offset: Int? = 0,
        txnId: String? = null
    ) = loadboardService.getFastagTransactionsByTollPlaza(tollPlazaId, dateTime, limit, offset, fastagId, txnId).convertResponse()

    /**
     * Get dispute form configuration
     */
    fun getDisputeFormConfig(disputeTypeCode: String) =
        loadboardService.getDisputeFormConfig(disputeTypeCode).convertResponse()
            .map { fields -> FormConfigResponse(fields) }

    /**
     * Submit dispute with multipart form data
     */
    fun submitDispute(
        txnId: MultipartBody.Part?,
        tollPlazaId: MultipartBody.Part?,
        refundAmount: MultipartBody.Part?,
        comment: MultipartBody.Part?,
        raisedAgainst: MultipartBody.Part?,
        additionalTxnId: MultipartBody.Part?=null,
        doc1: MultipartBody.Part?,
        doc2: MultipartBody.Part?,
        doc3: MultipartBody.Part?
    ): io.reactivex.Single<DisputeSubmissionResponse> {
        return loadboardService.submitDispute(
            txnId = txnId,
            tollPlazaId=tollPlazaId,
            refundAmount=refundAmount,
            comment=comment,
            raisedAgainst=raisedAgainst,
            additionalTxnId=additionalTxnId,
            uploadDoc1=doc1,
            uploadDoc2=doc2,
            uploadDoc3=doc3
        ).convertResponse()
    }

}
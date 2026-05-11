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
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.api.response.ServiceGroupsResponse
import com.delhivery.axle.api.response.ServiceRequirementsResponse
import com.delhivery.axle.api.response.OnboardingProgress
import com.delhivery.axle.api.response.DocumentSection
import com.delhivery.axle.api.response.DocumentRequirement
import com.delhivery.axle.api.response.DocumentActions
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.utils.ErrorLogger
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    /**
     * Get service groups for onboarding using Flow-based architecture.
     *
     * This method wraps the suspend API call in Flow with Resource states:
     * - Resource.Loading: Emitted immediately before API call
     * - Resource.Success: Emitted when API returns data successfully
     * - Resource.Failure: Emitted when API call fails with error details
     *
     * @param vendorId Vendor UUID for eligibility-based group visibility
     * @return Flow<Resource<ServiceGroupsResponse>> that emits Loading, then Success or Failure
     */
    fun getServiceGroupsFlow(vendorId: String): Flow<Resource<ServiceGroupsResponse>> {
        // TODO: Uncomment below when API is live
        // return safeApiCallFlow { loadboardService.getServiceGroups(vendorId) }

        // Mock data — remove this block when API is live
        return flow {
            emit(Resource.Loading)
            kotlinx.coroutines.delay(800) // simulate network delay
            emit(Resource.Success(
                ServiceGroupsResponse(
                    groups = listOf(
                        ServiceGroup(
                            groupId = "grp_loads",
                            groupName = "Load Services",
                            description = "Transportation and load services",
                            displayOrder = 1,
                            icon = "truck",
                            serviceCount = 4
                        ),
                        ServiceGroup(
                            groupId = "grp_finance",
                            groupName = "Financial Services",
                            description = "FASTag and Fuel Card products",
                            displayOrder = 2,
                            icon = "wallet",
                            serviceCount = 2
                        )
                    )
                )
            ))
        }.flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * Get service requirements for a specific service using Flow-based architecture.
     *
     * @param serviceId The service identifier (e.g., "svc_fastag")
     * @return Flow<Resource<ServiceRequirementsResponse>> that emits Loading, then Success or Failure
     */
    fun getServiceRequirementsFlow(serviceId: String): Flow<Resource<ServiceRequirementsResponse>> {
        // TODO: Uncomment below when API is live
        // return safeApiCallFlow { loadboardService.getServiceRequirements(serviceId) }

        // Mock data — remove this block when API is live
        return flow {
            emit(Resource.Loading)
            kotlinx.coroutines.delay(800) // simulate network delay
            emit(Resource.Success(
                ServiceRequirementsResponse(
                    serviceId = "svc_loads",
                    providerId = "axis",
                    configVersion = 3,
                    onboardingStatus = "PENDING",
                    progress = OnboardingProgress(
                        completedDocuments = 2,
                        requiredDocuments = 4,
                        completionPercent = 50
                    ),
                    sections = listOf(
                        DocumentSection(
                            section = "KYC",
                            documents = listOf(
                                DocumentRequirement(
                                    documentType = "PAN",
                                    label = "PAN Card",
                                    sequence = 1,
                                    isRequired = true,
                                    isVisible = true,
                                    isCompleted = true,
                                    isEnabled = true,
                                    status = "APPROVED",
                                    value = "ABCDE1234F",
                                    fileUrl = "s3://pan.pdf",
                                    reused = true,
                                    verificationMode = listOf("digital", "upload"),
                                    collectionMode = listOf("input", "upload"),
                                    actions = DocumentActions(canEdit = false, canReupload = false)
                                ),
                                DocumentRequirement(
                                    documentType = "AADHAAR",
                                    label = "Aadhaar",
                                    sequence = 2,
                                    isRequired = true,
                                    isVisible = true,
                                    isCompleted = false,
                                    isEnabled = false,
                                    dependsOn = "PAN",
                                    status = "PENDING",
                                    verificationMode = listOf("otp", "upload"),
                                    collectionMode = listOf("input", "upload"),
                                    actions = DocumentActions(canEdit = false, canReupload = false)
                                )
                            )
                        ),
                        DocumentSection(
                            section = "Business Details",
                            documents = listOf(
                                DocumentRequirement(
                                    documentType = "GST",
                                    label = "GST Details",
                                    sequence = 1,
                                    isRequired = true,
                                    isVisible = true,
                                    isCompleted = false,
                                    isEnabled = true,
                                    status = "UNDER_REVIEW",
                                    verificationMode = listOf("digital"),
                                    collectionMode = listOf("input"),
                                    actions = DocumentActions(canEdit = false, canReupload = false)
                                ),
                                DocumentRequirement(
                                    documentType = "BUSINESS_PROOF",
                                    label = "Business Proof",
                                    sequence = 2,
                                    isRequired = true,
                                    isVisible = true,
                                    isCompleted = false,
                                    isEnabled = true,
                                    status = "REJECTED",
                                    verificationMode = listOf("upload"),
                                    collectionMode = listOf("upload"),
                                    actions = DocumentActions(canEdit = true, canReupload = true)
                                )
                            )
                        ),
                        DocumentSection(
                            section = "Banking & Payments",
                            documents = listOf(
                                DocumentRequirement(
                                    documentType = "BANK_ACCOUNT",
                                    label = "Payment Details",
                                    sequence = 1,
                                    isRequired = true,
                                    isVisible = true,
                                    isCompleted = false,
                                    isEnabled = true,
                                    status = "PENDING",
                                    verificationMode = listOf("digital"),
                                    collectionMode = listOf("input"),
                                    actions = DocumentActions(canEdit = true, canReupload = false)
                                )
                            )
                        )
                    )
                )
            ))
        }.flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

}
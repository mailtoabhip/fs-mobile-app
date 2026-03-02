package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.request.GstDetailRequest
import com.delhivery.axle.api.request.GstNumberRequest
import com.delhivery.axle.api.request.PanVerificationRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.WalletTransactionItem
import com.delhivery.axle.api.response.WalletTransactionListResponse
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.google.gson.JsonObject
import javax.inject.Inject

class LoadboardRepository @Inject constructor(
    private val loadboardService: LoadBoardService
) : BaseRepository() {

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

    fun downloadFastagTransactions(tagId: String, from_date: String?,to_date: String?) = loadboardService.downloadFastagTransactions(tagId, from_date, to_date)

    fun getFastagTransactions(tagId: String, limit: Int, offset: Int) = loadboardService.getFastagTransactions(tagId, offset, limit).convertResponse()

    /**
     * Submit FASTag lead request
     */
    fun submitFastagLead(fastagLeadRequest: FastagLeadRequest) = loadboardService.submitFastagLead(fastagLeadRequest).convertResponse()

    /**
     * Fetch wallet details (real API call — uncomment when ready)
     */
    fun fetchWalletDetails() =
        loadboardService.fetchWalletDetails().convertResponse()

    /**
     * Mock wallet details — 404 wallet not exist (remove when switching to real API)
     */
//    fun fetchWalletDetails(): io.reactivex.Single<com.delhivery.axle.api.response.UserWalletResponse> =
//        io.reactivex.Single.error(
//            retrofit2.HttpException(
//                retrofit2.Response.error<Any>(
//                    404,
//                    okhttp3.ResponseBody.create(
//                        okhttp3.MediaType.parse("application/json"),
//                        """{"success":false,"error":{"message":"wallet not exist for this vendor","code":404},"data":{"success":false}}"""
//                    )
//                )
//            )
//        )

    /**
     * Create wallet (real API call — uncomment when ready)
     */
    fun createWallet() =
        loadboardService.createWallet().convertResponse()


    /**
     * Fetch wallet transaction listing (real API call — uncomment when ready)
     */
//    fun fetchWalletTransactionList(start: String, end: String, walletId: String) =
//        loadboardService.fetchWalletTransactionList(start, end, walletId).convertResponse()

    /**
     * Mock wallet transaction listing (remove when switching to real API)
     */
    fun fetchWalletTransactionList(start: String, end: String, walletId: String) =
        io.reactivex.Single.just(MockWalletData.getMockTransactions())

    /**
     * Fetch single transaction status (real API call — uncomment when ready)
     */
//    fun fetchTransactionStatus(start: String, txnId: String) =
//        loadboardService.fetchTransactionStatus(start, txnId).convertResponse()

    /**
     * Mock single transaction status (remove when switching to real API)
     */
    fun fetchTransactionStatus(start: String, txnId: String) =
        io.reactivex.Single.just(
            com.delhivery.axle.api.response.WalletTransactionStatusResponse(
                txnId = txnId,
                transactionType = "debit",
                amount = "102.0",
                status = "pending"
            )
        )

    /**
     * Fetch wallet recharge transactions (real API call — uncomment when ready)
     */
//    fun fetchWalletRechargeList(walletId: String, start: String, end: String) =
//        loadboardService.fetchWalletRechargeList(walletId, start, end).convertResponse()

    /**
     * Mock wallet recharge transactions (remove when switching to real API)
     */
    fun fetchWalletRechargeList(walletId: String, start: String, end: String) =
        io.reactivex.Single.just(
            com.delhivery.axle.api.response.WalletRechargeListResponse(
                walletId = walletId,
                total = 2,
                totalAmount = 700.5,
                openingBalance = 1000.0,
                recharges = listOf(
                    com.delhivery.axle.api.response.WalletRechargeItem(
                        rechargeId = "RID12345",
                        amount = 200.5,
                        status = "PENDING",
                        createdAt = "2026-02-01T12:00:00Z"
                    ),
                    com.delhivery.axle.api.response.WalletRechargeItem(
                        rechargeId = "RID12346",
                        amount = 500.0,
                        status = "SUCCESS",
                        createdAt = "2026-01-28T10:30:00Z"
                    ),
                    com.delhivery.axle.api.response.WalletRechargeItem(
                        rechargeId = "RID123463",
                        amount = 150.0,
                        status = "FAILURE",
                        createdAt = "2026-01-28T10:30:00Z"
                    )
                )
            )
        )

    /**
     * Fetch single recharge status (real API call — uncomment when ready)
     */
//    fun fetchRechargeStatus(rechargeId: String, start: String) =
//        loadboardService.fetchRechargeStatus(rechargeId, start).convertResponse()

    /**
     * Mock single recharge status (remove when switching to real API)
     */
    fun fetchRechargeStatus(rechargeId: String, start: String) =
        io.reactivex.Single.just(
            com.delhivery.axle.api.response.WalletRechargeStatusResponse(
                rechargeId = rechargeId,
                amount = "1200.0",
                status = "failed"
            )
        )

}

object MockWalletData {

    fun getMockTransactions(): WalletTransactionListResponse {
        return WalletTransactionListResponse(
            totalCount = 6,
            page = 1,
            perPage = 10,
            transactions = listOf(
                WalletTransactionItem(
                    transactionId = "MTX1752561136623085",
                    transactionType = "debit",
                    amount = "109.0",
                    status = "pending",
                    updatedWalletBalance = "892.0",
                    refType = "HYPERLOCAL",
                    createdAt = "2026-02-12T16:20:00.000Z"
                ),
                WalletTransactionItem(
                    transactionId = "MTX1752561136623086",
                    transactionType = "credit",
                    amount = "500.0",
                    status = "success",
                    updatedWalletBalance = "1392.0",
                    refType = "WALLET_RECHARGE",
                    createdAt = "2026-02-11T10:30:00.000Z"
                ),
                WalletTransactionItem(
                    transactionId = "MTX1752561136623087",
                    transactionType = "debit",
                    amount = "250.0",
                    status = "success",
                    updatedWalletBalance = "1142.0",
                    refType = "ORDER_PAYMENT",
                    createdAt = "2026-02-10T14:45:00.000Z"
                ),
                WalletTransactionItem(
                    transactionId = "MTX1752561136623088",
                    transactionType = "credit",
                    amount = "300.0",
                    status = "failure",
                    updatedWalletBalance = "1142.0",
                    refType = "REFUND",
                    createdAt = "2026-02-09T09:15:00.000Z"
                ),
                WalletTransactionItem(
                    transactionId = "MTX1752561136623089",
                    transactionType = "debit",
                    amount = "120.0",
                    status = "pending",
                    updatedWalletBalance = "1022.0",
                    refType = "FASTAG_RECHARGE",
                    createdAt = "2026-02-08T18:00:00.000Z"
                ),
                WalletTransactionItem(
                    transactionId = "MTX1752561136623090",
                    transactionType = "debit",
                    amount = "75.0",
                    status = "failure",
                    updatedWalletBalance = "1022.0",
                    refType = "HYPERLOCAL",
                    createdAt = "2026-02-07T12:30:00.000Z"
                )
            )
        )
    }
}
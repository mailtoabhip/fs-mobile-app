package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.*
import com.delhivery.axle.api.request.GstDetailRequest
import com.delhivery.axle.api.request.GstNumberRequest
import com.delhivery.axle.api.request.PanVerificationRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.RcVerificationResponse
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

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
    ).convertResponse()


    /**
     * update user
     */
    fun updateUser(updateUserRequest: UpdateUserRequest) =
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
                Pair(false, "Account not created")
            }

    fun validatePanNumber(panNumber:String)= loadboardService.validatePanNumber(PanVerificationRequest(panNumber)).convertResponse()

}
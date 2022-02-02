package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.AddAddressVerificationRequest
import com.delhivery.axle.api.request.GstDetailRequest
import com.delhivery.axle.api.request.GstNumberRequest
import com.delhivery.axle.api.request.UpdateAddressVerificationRequest
import com.delhivery.axle.api.request.PanVerificationRequest
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.service.LoadBoardService
import com.delhivery.axle.utils.extensions.convertResponse
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

    fun addAlternateAddress(phone_number: String,business_address:String,proof_document_type:String,proof_document_url:String) = loadboardService.addNewAddress(AddAddressVerificationRequest(phone_number,business_address,proof_document_type,proof_document_url)).convertResponse()

    fun updateCommunicationAddress(phone_number: String,business_address:String,is_same_as_gst:Boolean) = loadboardService.updateNewAddress(
        UpdateAddressVerificationRequest(phone_number,business_address,is_same_as_gst)
    ).convertResponse()


    /**
     * validate pan number
     */
    fun validatePanNumber(panNumber:String)= loadboardService.validatePanNumber(PanVerificationRequest(panNumber)).convertResponse()



    /**
     * update user pan number
     */
    fun updateUser(phoneNumber:String,panNumber:String?)= loadboardService.updateUser(UpdateUserRequest(phoneNumber,panNumber)).convertResponse()

}
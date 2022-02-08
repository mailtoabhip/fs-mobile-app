package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.*
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

    fun addAddress(phone_number: String,address:String,proof_document_type:String,document_url:List<String>,address_type:String) = loadboardService.addAddress(
        AddAddressRequest(phone_number,address,proof_document_type,document_url,address_type)
    ).convertResponse()

    fun updateCommunicationAddress(business_address:String,is_same_as_gst:Boolean,phone_number: String) = loadboardService.updateNewAddress(
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
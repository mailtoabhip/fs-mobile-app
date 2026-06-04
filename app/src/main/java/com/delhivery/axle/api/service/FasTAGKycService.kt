package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.KycInitiateRequest
import com.delhivery.axle.api.request.KycVerifyRequest
import com.delhivery.axle.api.response.BarcodeLookupResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.KycInitiateResponse
import com.delhivery.axle.api.response.KycOnboardValidateResponse
import com.delhivery.axle.api.response.KycTypesResponse
import com.delhivery.axle.api.response.KycVerifyResponse
import com.delhivery.axle.api.response.ProductBarcodeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FasTAGKycService {

    @GET("/fastag/tag-issuance/v1/kyc/onboard-validate")
    suspend fun kycOnboardValidate(
        @Query("bank_code") bankCode: String
    ): BaseResponse<KycOnboardValidateResponse>

    @GET("/fastag/tag-issuance/v1/kyc/types")
    suspend fun getKycTypes(
        @Query("bank_code") bankCode: String
    ): BaseResponse<KycTypesResponse>

    @POST("/fastag/tag-issuance/v1/kyc/initiate")
    suspend fun initiateKyc(
        @Body request: KycInitiateRequest
    ): BaseResponse<KycInitiateResponse>

    @POST("/fastag/tag-issuance/v1/kyc/verify-and-create")
    suspend fun verifyAndCreateKyc(
        @Body request: KycVerifyRequest
    ): BaseResponse<KycVerifyResponse>

    /**
     * Lookup barcode from dispatch table.
     * TODO Remove it
     */
    @GET("/fastag/tag-issuance/v1/barcode")
    suspend fun barcodeLookup(
        @Query("order_id") orderId: String,
        @Query("order_item_id") orderItemId: Int,
        @Query("vehicle_class") vehicleClass: String
    ): BaseResponse<BarcodeLookupResponse>

    /**
     * Search products and barcodes from IDFC.
     */
    @POST("/fastag/tag-issuance/v1/issuance/product-barcode")
    suspend fun searchProductBarcode(
        @Body request: com.delhivery.axle.api.request.ProductBarcodeRequest
    ): BaseResponse<ProductBarcodeResponse>

    /**
     * Generate consent OTP for tag mapping.
     */
    @POST("/fastag/tag-issuance/v1/issuance/generate-otp")
    suspend fun generateOtp(
        @Body request: com.delhivery.axle.api.request.GenerateOtpRequest
    ): BaseResponse<Any>
}

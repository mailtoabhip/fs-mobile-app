package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.KycInitiateRequest
import com.delhivery.axle.api.request.KycVerifyRequest
import com.delhivery.axle.api.response.BarcodeLookupResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.FastagImageUploadResponse
import com.delhivery.axle.api.response.FastagImageValidateResponse
import com.delhivery.axle.api.response.KycInitiateResponse
import com.delhivery.axle.api.response.KycOnboardValidateResponse
import com.delhivery.axle.api.response.KycTypesResponse
import com.delhivery.axle.api.response.KycVerifyResponse
import com.delhivery.axle.api.response.OrderItem
import com.delhivery.axle.api.response.ProductBarcodeResponse
import com.delhivery.axle.api.response.RcProcessResponse
import com.delhivery.axle.api.response.RcProcessStatusResponse
import com.delhivery.axle.api.response.VehicleImageProcessResponse
import com.delhivery.axle.api.response.VehicleImageProcessStatusResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FasTAGKycService {

    @GET("/fastag/v1/kyc/onboard-validate")
    suspend fun kycOnboardValidate(
        @Query("bank_code") bankCode: String
    ): BaseResponse<KycOnboardValidateResponse>

    @GET("/fastag/v1/kyc/types")
    suspend fun getKycTypes(
        @Query("bank_code") bankCode: String
    ): BaseResponse<KycTypesResponse>

    @POST("/fastag/v1/kyc/initiate")
    suspend fun initiateKyc(
        @Body request: KycInitiateRequest
    ): BaseResponse<KycInitiateResponse>

    @POST("/fastag/v1/kyc/verify-and-create")
    suspend fun verifyAndCreateKyc(
        @Body request: KycVerifyRequest
    ): BaseResponse<KycVerifyResponse>

    @GET("/fastag/v1/order/{order_id}/items")
    suspend fun getOrderItems(
        @Path("order_id") orderId: String
    ): BaseResponse<List<OrderItem>>

    @Multipart
    @POST("/fastag/v1/issuance/rc-process")
    suspend fun uploadRcImages(
        @Part rcFront: MultipartBody.Part,
        @Part rcBack: MultipartBody.Part,
        @Part orderId: MultipartBody.Part,
        @Part orderItemId: MultipartBody.Part
    ): BaseResponse<RcProcessResponse>

    @GET("/fastag/v1/issuance/rc-process/{job_id}/status")
    suspend fun getRcProcessStatus(
        @Path("job_id") jobId: String
    ): BaseResponse<RcProcessStatusResponse>

    @Multipart
    @POST("/fastag/v1/issuance/vehicle-images-process")
    suspend fun uploadVehicleImages(
        @Part vehicleFront: MultipartBody.Part,
        @Part vehicleSide: MultipartBody.Part,
        @Part orderId: MultipartBody.Part,
        @Part orderItemId: MultipartBody.Part,
        @Part journeyId: MultipartBody.Part
    ): BaseResponse<VehicleImageProcessResponse>

    @GET("/fastag/v1/issuance/vehicle-images-process/{job_id}/status")
    suspend fun getVehicleImageProcessStatus(
        @Path("job_id") jobId: String
    ): BaseResponse<VehicleImageProcessStatusResponse>

    @Multipart
    @POST("/fastag/v1/issuance/fastag-image")
    suspend fun uploadFastagImage(
        @Part fastagImage: MultipartBody.Part,
        @Part journeyId: MultipartBody.Part,
        @Part orderId: MultipartBody.Part,
        @Part orderItemId: MultipartBody.Part
    ): BaseResponse<FastagImageUploadResponse>

    @POST("/fastag/v1/issuance/fastag-image/validate")
    suspend fun validateFastagImage(
        @Body request: com.delhivery.axle.api.request.FastagImageValidateRequest
    ): BaseResponse<FastagImageValidateResponse>

    /**
     * Lookup barcode from dispatch table.
     * TODO Remove it
     */
    @GET("/fastag/v1/barcode")
    suspend fun barcodeLookup(
        @Query("order_id") orderId: String,
        @Query("order_item_id") orderItemId: Int,
        @Query("vehicle_class") vehicleClass: String
    ): BaseResponse<BarcodeLookupResponse>

    /**
     * Search products and barcodes from IDFC.
     */
    @POST("/fastag/v1/issuance/product-barcode")
    suspend fun searchProductBarcode(
        @Body request: com.delhivery.axle.api.request.ProductBarcodeRequest
    ): BaseResponse<ProductBarcodeResponse>

    /**
     * Generate consent OTP for tag mapping.
     */
    @POST("/fastag/v1/issuance/generate-otp")
    suspend fun generateOtp(
        @Body request: com.delhivery.axle.api.request.GenerateOtpRequest
    ): BaseResponse<Any>
}

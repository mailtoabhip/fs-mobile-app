package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.ConfirmCollectionRequest
import com.delhivery.axle.api.request.KycInitiateRequest
import com.delhivery.axle.api.request.KycVerifyRequest
import com.delhivery.axle.api.request.ValidateSalesRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ConfirmCollectionResponse
import com.delhivery.axle.api.response.FastagOrdersResponse
import com.delhivery.axle.api.response.KycOnboardValidateResponse
import com.delhivery.axle.api.response.KycInitiateResponse
import com.delhivery.axle.api.response.KycTypesResponse
import com.delhivery.axle.api.response.KycVerifyResponse
import com.delhivery.axle.api.response.ValidateSalesCodeResponse
import com.delhivery.axle.api.response.VehicleCheckResponse
import com.delhivery.axle.api.response.VehicleClassResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FasTAGIssuanceService {
    @POST("/fastag/tag-issuance/v1/sales-code/validate")
    suspend fun validateSalesCode(
        @Body validateSalesRequest: ValidateSalesRequest
    ): BaseResponse<ValidateSalesCodeResponse>

    @GET("/fastag/tag-issuance/v1/vehicle-classes")
    suspend fun getVehicleClasses(): BaseResponse<VehicleClassResponse>

    @GET("/fastag/tag-issuance/v1/orders/by-vendor")
    suspend fun getOrdersByVendor(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): BaseResponse<FastagOrdersResponse>

    @POST("/fastag/tag-issuance/v1/order/{order_id}/confirm-collection")
    suspend fun confirmCollection(
        @Path("order_id") orderId: String,
        @Body request: ConfirmCollectionRequest
    ): BaseResponse<ConfirmCollectionResponse>

    @GET("/fastag/tag-issuance/v1/vehicle/check")
    suspend fun checkVehicle(
        @Query("vehicle_number") vehicleNumber: String
    ): BaseResponse<VehicleCheckResponse>

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
}
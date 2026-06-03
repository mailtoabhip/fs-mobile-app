package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.KycInitiateRequest
import com.delhivery.axle.api.request.KycVerifyRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.KycInitiateResponse
import com.delhivery.axle.api.response.KycOnboardValidateResponse
import com.delhivery.axle.api.response.KycTypesResponse
import com.delhivery.axle.api.response.KycVerifyResponse
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
}

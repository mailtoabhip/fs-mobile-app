package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.PanVerificationRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.PanVerificationResponse
import com.delhivery.axle.api.response.TransactionBidsResponseBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface LoadBoardService {
    /**
     * verify Pan Card Number
     */
    @GET("/validate_pan_card")
    fun validatePanNumber(@Body panVerificationRequest: PanVerificationRequest)
            : Single<BaseResponse<PanVerificationResponse>>
}
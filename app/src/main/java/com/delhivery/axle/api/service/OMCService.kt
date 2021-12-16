package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.OMCRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.OMCResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface OMCService  {
    @POST("/card/")
    fun omcCard(
        @Body request: OMCRequest
    ):Single<BaseResponse<OMCResponse>>
}
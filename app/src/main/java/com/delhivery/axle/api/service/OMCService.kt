package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import io.reactivex.Single
import retrofit2.http.POST

interface OMCService  {
    @POST("/card/")
    fun omcCard(

    ):Single<BaseResponse<Any>>
}
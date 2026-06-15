package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.OMCRequest
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.OMCResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface OMCService  {
    @POST("/card/")
    fun omcCard(
        @Body request: OMCRequest
    ):Single<BaseResponse<OMCResponse>>
}
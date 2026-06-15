package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.GetSupplierRewardsResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface TruckService {

    /**
     * Get all rewards
     */
    @POST("get_verification_data")
    fun getSupplierRewards(@Body request: JsonObject):
        Single<BaseResponse<GetSupplierRewardsResponse>>
}
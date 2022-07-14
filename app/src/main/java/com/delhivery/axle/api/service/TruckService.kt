package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TruckResponse
import com.delhivery.axle.api.response.GetSupplierRewardsResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TruckService {

    @GET("v2/orion_trucks")
    fun getTrucks(): Single<BaseResponse<List<TruckResponseArray>>>

    /**
     * Get all rewards
     */
    @POST("get_verification_data")
    fun getSupplierRewards(@Body request: JsonObject):
        Single<BaseResponse<GetSupplierRewardsResponse>>
}
package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ChargesResponse
import com.delhivery.axle.api.response.DNResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface PayableService {
    /**
     * Get List Charges
     */
    @POST("list_charges")
    fun fetchChargeList(@Body payload: JsonObject): Single<BaseResponse<List<ChargesResponse>>>

    @POST("list_dns")
    fun fetchDNList(@Body payload: JsonObject): Single<BaseResponse<List<DNResponse>>>
}
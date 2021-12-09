package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TruckResponse
import com.delhivery.axle.api.response.TruckResponseArray
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface TruckService {

    @GET("orion_trucks")
    fun getTrucks(): Single<BaseResponse<List<TruckResponseArray>>>

}
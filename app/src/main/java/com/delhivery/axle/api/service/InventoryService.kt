package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.AddVehicle
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface InventoryService {

    @POST("add")
    fun addInventory(
        @Body request:AddVehicle
    ): Single<BaseResponse<HomeTrucksRequestItemData>>
}
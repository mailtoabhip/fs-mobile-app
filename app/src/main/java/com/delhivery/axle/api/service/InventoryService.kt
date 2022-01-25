package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.DeactivateTruckRequest
import com.delhivery.axle.api.request.DeleteTruckRequest
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface InventoryService {

    @POST("list_sp_trucks")
    fun getInventories(
        @Body request: JsonObject
    ):Single<BaseResponse<List<HomeTrucksRequestItemData>>>

    @POST("add_sp_truck")
    fun addInventory(
        @Body request: JsonObject
    ): Single<BaseResponse<HomeTrucksRequestItemData>>

    @PATCH("update_sp_truck")
    fun activateTruck(
        @Body request: JsonObject
    ):Single<BaseResponse<HomeTrucksRequestItemData>>

    @PATCH("update_sp_truck")
    fun editTruck(
        @Body request: JsonObject
    ): Single<BaseResponse<HomeTrucksRequestItemData>>

    @PATCH("update_sp_truck")
    fun deActivateTruck(
        @Body request: DeactivateTruckRequest
    ): Single<BaseResponse<HomeTrucksRequestItemData>>

    @POST("delete_sp_truck")
    fun deleteTruck(
        @Body request: DeleteTruckRequest
    ): Single<BaseMessageResponse>
}
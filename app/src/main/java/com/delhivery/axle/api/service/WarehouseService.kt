package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.CreateFuelCardRequest
import com.delhivery.axle.api.request.WarehouseRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.WarehouseDetailResponse
import com.delhivery.axle.api.response.WarehouseIndentResponse
import io.reactivex.Single
import retrofit2.http.*

/**
 * Handle network calls to Warehouse Service
 */
interface WarehouseService {

  /**
   * Warehouse details
   */
  @GET("warehouses/{clientId}/")
  fun fetchWarehouseDetails(
    @Path("clientId") clientId: String,
    @Query("warehouse_name") warehouseName: String
  ): Single<BaseResponse<WarehouseDetailResponse>>

  /**
   * Warehouse details
   */
  @POST("warehouse_details/")
  fun postWarehouseDetails(
          @Body payload: WarehouseRequest
  ): Single<BaseResponse<WarehouseIndentResponse>>

}
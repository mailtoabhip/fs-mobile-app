package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.WarehouseDetailResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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

}
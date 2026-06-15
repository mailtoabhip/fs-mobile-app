package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.WarehouseRequest
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.WarehouseDetailResponse
import com.dfd.delfin.api.response.WarehouseIndentResponse
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
package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.UpdateVehicleDetailsRequest
import com.dfd.delfin.api.response.FacilityAddressResponse
import com.dfd.delfin.api.response.PlacementsLoadDataResponse
import com.dfd.delfin.api.response.RecommendedDriverResponse
import com.dfd.delfin.api.response.TPSBaseResponse
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface TPSService{

    @GET("placement_dashboard/contracts")
    fun placementLoads(@Query("supplier_uuid") supplierUuid: String)
            : Single<TPSBaseResponse<PlacementsLoadDataResponse>>

    @PUT("placement_dashboard/contracts/update_placement_details")
    fun updateVehicleDetails(@Body updateVehicleDetailsRequest: UpdateVehicleDetailsRequest)
            : Single<TPSBaseResponse<JSONObject>>

    @GET("placement_dashboard/get_center_details")
    fun getFacilityAddress(@Query("center_code") centerCode: String)
            : Single<TPSBaseResponse<FacilityAddressResponse>>

    @GET("placement_dashboard/drivers/recommended")
    fun getRecentDriverNameOnVehicle(@Query("vehicle_registration_number") vehicleNumber: String)
            : Single<TPSBaseResponse<RecommendedDriverResponse>>?

    /**
     * PlacementDetails Wrapper API
     */
    @GET("/placement_dashboard/contracts/get_placement_details")
    fun getPlacementDetails(
        @Query("placement_type") placementType: String,
        @Query("transaction_id") transactionId:String?=null,
        @Query("contract_code") contractCode:String?=null
    ): Single<TPSBaseResponse<HomeBidsRequestItemData>>
}



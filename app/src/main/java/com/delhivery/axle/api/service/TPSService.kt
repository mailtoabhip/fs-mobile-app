package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.UpdateVehicleDetailsRequest
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.DriverDataResponse
import com.delhivery.axle.api.response.FacilityAddressResponse
import com.delhivery.axle.api.response.PlacementsLoadDataResponse
import com.delhivery.axle.api.response.TPSBaseResponse
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
            : Single<TPSBaseResponse<List<DriverDataResponse>>>?
}



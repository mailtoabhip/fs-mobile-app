package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName


data class DeactivateTruckRequest(
    @SerializedName("inventory_uuid") var inventoryUuid: String,
    @SerializedName("inventory_status" ) var inventoryStatus: String,
    @SerializedName("deactivate_reason") var deactivateReason: String
)

data class ActivateTruckRequest(
    @SerializedName("inventory_uuid") var inventoryUuid: String,
    @SerializedName("inventory_status" ) var inventoryStatus: String
)

data class UpdateTruck(
    @SerializedName("supplier_id") var supplierId: String,
    @SerializedName("truck_number") var truckNumber: String,
    @SerializedName("ownership") var ownership :String? = null,
    @SerializedName("current_city_name") var currentCityName: String? = null,
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("unloading_destination") var unloadingDestination: String? = null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null
)

data class AddVehicle(
    @SerializedName("supplier_id") var supplierId: String,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("body_type") val bodyType: String,
    @SerializedName("truck_number") var truckNumber: String,
    @SerializedName("ownership") var ownership :String,
    @SerializedName("truck_size") var truckSize: String,
    @SerializedName("capacity") var capacity: String,
    @SerializedName("current_city_name") var currentCityName: String? = null,
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("unloading_destination") var unloadingDestination: String? = null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null
)
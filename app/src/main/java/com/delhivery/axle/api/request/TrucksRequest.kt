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
    @SerializedName("vehicle_number") var vehicleNumber: String,
    @SerializedName("ownership") var ownership :String? = null,
    @SerializedName("current_city") var currentCityName: String? = null,
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("destination_city") var unloadingDestination: String? = null,
    @SerializedName("destination_city_code") var unloadingDestinationCode: String? =null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null
)

data class AddVehicle(
    @SerializedName("supplier_id") var supplierId: String,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("body_type") val bodyType: String,
    @SerializedName("vehicle_number") var vehicleNumber: String,
    @SerializedName("ownership") var ownership :String,
    @SerializedName("truck_uuid") val truckSize: String,
    @SerializedName("capacity") var capacity: Double,
    @SerializedName("current_city") var currentCityName: String? = null,
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("destination_city") var unloadingDestination: String? = null,
    @SerializedName("destination_city_code") var unloadingDestinationCode: String? =null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null
)
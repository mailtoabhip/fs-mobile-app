package com.delhivery.axle.api.request

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class DeactivateTruckRequest(
    @SerializedName("uuid") var inventoryUuid: String,
    @SerializedName("inventory_status" ) var inventoryStatus: String,
    @SerializedName("update_type") var updateType: String,
    @SerializedName("deactivate_reason") var deactivateReason: String,
    @SerializedName("originator") var originator: String = "axle-app"
)

data class DeleteTruckRequest(
    @SerializedName("uuid") var inventoryUuid: String
)

class UpdateTruck(
    var inventoryId: String,
    var updateType: String,
    var currentCityName: String,
    var currentCityCode: String,
    var unloadingDestination: String,
    var unloadingDestinationCode: String,
    var sourcedAs: String,
    var inventoryStatus: String?=null,
    var unloadingDestinationAmount: Double ?= null,
    var unloadingDestinationRate: Double ?= null,
    var originator: String = "axle-app"
){
    fun getRequest():JsonObject{
        val jsonObject = JsonObject()
        jsonObject.addProperty("uuid", inventoryId)
        jsonObject.addProperty("update_type", updateType)
        jsonObject.addProperty("current_city", currentCityName)
        jsonObject.addProperty("current_city_code", currentCityCode)
        jsonObject.addProperty("destination_city", unloadingDestination)
        jsonObject.addProperty("destination_city_code", unloadingDestinationCode)
        if(sourcedAs == "FTL") {
            unloadingDestinationAmount?.let {
                jsonObject.addProperty(
                    "unloading_destination_amount",
                    unloadingDestinationAmount
                )
            }
        }
        else if(sourcedAs == "PMT") {
            unloadingDestinationRate?.let {
                jsonObject.addProperty(
                    "unloading_destination_rate",
                    unloadingDestinationCode
                )
            }
        }
        inventoryStatus?.let { jsonObject.addProperty("inventory_status", inventoryStatus) }
        jsonObject.addProperty("originator", originator)

        return jsonObject
    }

}

class AddVehicle(
    var supplierId: String,
    val supplierName: String,
    val bodyType: String,
    var vehicleNumber: String,
    var ownership :String,
    val truckSize: String,
    var capacity: Double,
    var currentCityName: String,
    var currentCityCode: String,
    var unloadingDestination: String,
    var unloadingDestinationCode: String,
    var sourcedAs: String,
    var unloadingDestinationAmount: Double?= null,
    var unloadingDestinationRate: Double?= null
){
    fun getRequest():JsonObject{
        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", supplierId)
        jsonObject.addProperty("supplier_name",supplierName)
        jsonObject.addProperty("body_type", bodyType)
        jsonObject.addProperty("vehicle_number", vehicleNumber)
        jsonObject.addProperty("ownership",ownership)
        jsonObject.addProperty("truck_uuid", truckSize)
        jsonObject.addProperty("capacity", capacity)
        jsonObject.addProperty("current_city", currentCityName)
        jsonObject.addProperty("current_city_code", currentCityCode)
        jsonObject.addProperty("destination_city", unloadingDestination)
        jsonObject.addProperty("destination_city_code", unloadingDestinationCode)
        if(sourcedAs == "FTL") {
            unloadingDestinationAmount?.let {
                jsonObject.addProperty(
                    "unloading_destination_amount",
                    unloadingDestinationAmount
                )
            }
        }
        else if(sourcedAs == "PMT") {
            unloadingDestinationRate?.let {
                jsonObject.addProperty(
                    "unloading_destination_rate",
                    unloadingDestinationCode
                )
            }
        }
        return jsonObject
    }
}
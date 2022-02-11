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
    var originClusterId: String,
    var destinationClusterId: String,
    var demandType: String?= null,
    var price:Double = 0.0,
    var inventoryStatus: String?=null,
    var ownership: String?= null,
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
        jsonObject.addProperty("origin_cluster_id", originClusterId)
        jsonObject.addProperty("destination_cluster_id", destinationClusterId)
        if(sourcedAs == "FTL" ) {
            jsonObject.addProperty(
                "unloading_destination_amount",
                price
            )
        }
        else if(sourcedAs == "PMT") {
                jsonObject.addProperty(
                    "unloading_destination_rate",
                    price
                )
        }
        inventoryStatus?.let { jsonObject.addProperty("inventory_status", inventoryStatus) }
        ownership?.let{ jsonObject.addProperty("ownership", it)}
        demandType?.let { jsonObject.addProperty("demand_type",it) }
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
    var originClusterId: String,
    var destinationClusterId: String,
    var sourcedAs: String,
    var demandType: String,
    var price: Double= 0.0
){
    fun getRequest():JsonObject{
        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", supplierId)
        jsonObject.addProperty("supplier_name",supplierName)
        jsonObject.addProperty("truck_type", bodyType)
        jsonObject.addProperty("vehicle_number", vehicleNumber)
        jsonObject.addProperty("ownership",ownership)
        jsonObject.addProperty("truck_uuid", truckSize)
        jsonObject.addProperty("capacity", capacity)
        jsonObject.addProperty("current_city", currentCityName)
        jsonObject.addProperty("current_city_code", currentCityCode)
        jsonObject.addProperty("destination_city", unloadingDestination)
        jsonObject.addProperty("destination_city_code", unloadingDestinationCode)
        jsonObject.addProperty("origin_cluster_id", originClusterId)
        jsonObject.addProperty("destination_cluster_id", destinationClusterId)
        jsonObject.addProperty("demand_type", demandType)
        if(sourcedAs == "FTL" && price != 0.0) {
            jsonObject.addProperty(
                "unloading_destination_amount",
                price
            )

        }
        else if(sourcedAs == "PMT" && price != 0.0) {
                jsonObject.addProperty(
                    "unloading_destination_rate",
                    price
                )
        }
        return jsonObject
    }
}
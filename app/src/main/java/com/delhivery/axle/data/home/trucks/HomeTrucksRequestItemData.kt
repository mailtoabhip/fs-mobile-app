package com.delhivery.axle.data.home.trucks

import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class HomeTrucksRequestItemData(
    @SerializedName("inventory_uuid") val inventoryId : String,
    @SerializedName("supplier_id") var supplierId: String,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("vehicle_number") var vehicleNumber: String,
    @SerializedName("body_type") val truckType: String,
    @SerializedName("ownership") var ownership :String,
    @SerializedName("truck_uuid") val truckSize: String,
    @SerializedName("capacity") val capacity: Double,
    @SerializedName("current_city") var currentCityName: String? = null,
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("destination_city") var unloadingDestination: String? = null,
    @SerializedName("destination_city_code") var unloadingDestinationCode: String? =null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null,
    @SerializedName("status") var status : String,
    @SerializedName("last_deactivated_at") var lastDeactivateTime: String,
    @SerializedName("last_deactivate_reason") var lastDeactivateReason: String
) : BaseKeyTypeModel<String>(){

    override fun key()= inventoryId

    @DrawableRes
    fun truckImage() : Int{
        return if(truckType =="closed")
            R.drawable.ic_closed
        else if( truckType== "open")
            R.drawable.ic_open
        else
            R.drawable.ic_trailer
    }


    fun truckName(): String {
        return if (truckType == "closed")
            "Container"
        else if (truckType == "open")
           "Open Body"
        else
            "Trailer"
    }

    fun truckCapacity():String  = "$capacity MT"

    fun truckSize(): String = truckSize

}

const val HomeTrucksRequestAction_ViewDetails = "truck_details"

const val HomeTrucksRequestAction_EditTruck = "edit_truck"

const val HomeTrucksRequestAction_ActivateTruck = "activate_truck"

data class TruckFrequentItem(
    val truckType: String,
    val truckSize: String,
    val capacity: Double,
    val minCap: Double,
    val maxCap: Double,
    val sourcedAs: String
    ) {


    @DrawableRes
    fun truckImage(): Int {
        return if (truckType == "closed")
            R.drawable.ic_closed
        else if (truckType == "open")
            R.drawable.ic_open
        else
            R.drawable.ic_trailer
    }


    fun truckName(): String {
        return if (truckType == "closed")
            "Container"
        else if (truckType == "open")
            "Open Body"
        else
            "Trailer"
    }

    fun truckCapacity(): String = "$capacity MT"

    fun truckSize(): String = truckSize
}
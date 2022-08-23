package com.delhivery.axle.data.home.trucks

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.extensions.not
import com.google.gson.annotations.SerializedName
import java.util.*

data class HomeTrucksRequestItemData(
    @SerializedName("uuid") val inventoryId : String,
    @SerializedName("supplier_id") var supplierId: String,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("vehicle_number") var vehicleNumber: String,
    @SerializedName("truck_type") val truckType: String,
    @SerializedName("ownership") var ownership :String?,
    @SerializedName("truck_uuid") val truckSize: String,
    @SerializedName("capacity") val capacity: Double,
    @SerializedName("current_city") var currentCityName: String? = "",
    @SerializedName("current_city_code") var currentCityCode: String? = null,
    @SerializedName("destination_city") var unloadingDestination: String? = "",
    @SerializedName("destination_city_code") var unloadingDestinationCode: String? =null,
    @SerializedName("unloading_destination_amount") var unloadingDestinationAmount: Double? = null,
    @SerializedName("unloading_destination_rate") var unloadingDestinationRate: Double? = null,
    @SerializedName("last_deactivated_at") var lastDeactivateTime: String,
    @SerializedName("last_deactivate_reason") var lastDeactivateReason: String,
    @SerializedName("latest_inventory_uuid") var latestUUID: String? = null,
    @SerializedName("latest_inventory_status") var latestStatus: String? = null,
    @SerializedName("created_at") var createdAt: String,
    @SerializedName("created_by") var createdBy: String,
    @SerializedName("origin_cluster_id") var originClusterId: String,
    @SerializedName("destination_cluster_id") var destinationClusterId: String,
    @SerializedName("sourced_as") var sourcedAs: String? =null,
    @SerializedName("res_offer") var resOffer: Triple<Pair<Boolean?,String?>, String?, Pair<String?,String?>>? = Triple(Pair(null, null), null, Pair(null,null))
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

    fun truckNumber() = vehicleNumber

    fun ownership() = (((ownership?.split("_"))?.toTypedArray())?.joinToString(" "))?.capitalize()

    fun truckSizeAndCap() = truckSize()+ "-" + truckCapacity()

    fun originCity() = currentCityName?.capitalize()

    fun destinationCity() = unloadingDestination?.capitalize()


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

    @ColorRes
    fun statusColor() = if(latestStatus == "Free")
        R.color.bid_placed_green
      else if(latestStatus == "Active")
        R.color.bid_placed_green
    else R.color.bid_placed_red

    fun statusText()= if(latestStatus == "Free")
        "Looking for Load"
    else if(latestStatus == "Active")
        "In a Trip"
       else ""

    fun statusVisibilty() = if(latestStatus == "not_available")
        View.VISIBLE
    else
        View.GONE

    fun locationVisibility() =  if(latestStatus == "Free")
        View.VISIBLE
    else
        View.GONE


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
package com.delhivery.axle.data.home.trucks

import androidx.annotation.DrawableRes
import com.delhivery.axle.R
import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class HomeTrucksRequestItemData(
    @SerializedName("truck_id") val truckId : String,
    @SerializedName("truck_type") val truckType: String,
    @SerializedName("truck_size") val truckSize: String,
    @SerializedName("capacity") val capacity: Double
) : BaseKeyTypeModel<String>(){

    override fun key()= truckId

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

data class TruckText(
    val truckValue: String
)
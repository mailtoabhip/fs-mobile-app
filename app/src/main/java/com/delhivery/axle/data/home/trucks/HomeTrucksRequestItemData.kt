package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class HomeTrucksRequestItemData(
    @SerializedName("truck_id") val truckId : String
) : BaseKeyTypeModel<String>(){

    override fun key()= truckId

}
const val HomeTrucksRequestAction_ViewDetails = "truck_details"

const val HomeTrucksRequestAction_EditTruck = "edit_truck"

const val HomeTrucksRequestAction_ActivateTruck = "activate_truck"
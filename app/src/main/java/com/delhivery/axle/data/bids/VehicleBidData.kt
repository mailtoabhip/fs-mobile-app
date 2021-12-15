package com.delhivery.axle.data.bids

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class VehicleBidData(

    @SerializedName("bid_price") var bidPrice: Double,
    @SerializedName("truck_capacity")var truckCapacity: Double,
    @SerializedName("truck_count")var truckCount: Int,
    @SerializedName("vehicle_type")var vehicleType: String,
    @SerializedName("freight_cost")var freightCost: Double =0.0
)

data class ModifyVehicleData(
    @SerializedName("bid_price") var bidPrice: Double,
    @SerializedName("truck_capacity")var truckCapacity: Double,
    @SerializedName("truck_count")var truckCount: Int,
    @SerializedName("vehicle_type")var vehicleType: String,
    @SerializedName("sub_action")var subAction: String?,
    @SerializedName("bid_id")var bidId: List<String>?,
    @SerializedName("is_bid_price_change") var priceFlag: Boolean = false
)


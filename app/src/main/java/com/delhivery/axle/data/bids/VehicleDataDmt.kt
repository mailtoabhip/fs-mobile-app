package com.delhivery.axle.data.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class VehicleDataDmt(

    var bid_price: Double,
    var truck_capacity: Double,
    var truckCount: Int,
    var vehicleType: String,
    var frieght_cost: Double


    ): BaseKeyTypeModel<String>() {
    override fun key(): String {
        TODO("Not yet implemented")
    }


}

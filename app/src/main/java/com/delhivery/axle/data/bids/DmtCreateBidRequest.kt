package com.delhivery.axle.data.bids

import com.delhivery.axle.data.BaseKeyTypeModel

data class DmtCreateBidRequest(
    var bidding_type: String,
    var originator: String,
    var supplier_id: String,
    var unallocated_load: Double,
    var supplier_name: String,
    var transaction_id: String,
    var vehicle_data: VehicleDataDmt


): BaseKeyTypeModel<String>() {
    override fun key(): String {
        TODO("Not yet implemented")
    }


}

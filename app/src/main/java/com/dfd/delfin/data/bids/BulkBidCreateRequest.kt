package com.dfd.delfin.data.bids

import com.dfd.delfin.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class BulkBidCreateRequest(
    @SerializedName("bidding_type")var biddingType: String,
    @SerializedName("originator")var originator: String,
    @SerializedName("supplier_id")var supplierId: String,
    @SerializedName("unallocated_load")var unallocatedLoad: Double,
    @SerializedName("supplier_name")var supplierName: String,
    @SerializedName("transaction_id")var transactionId: String,
    @SerializedName("vehicle_data")var vehicleData: List<VehicleBidData>

): BaseKeyTypeModel<String>() {
    override fun key(): String {
        TODO("Not yet implemented")
    }


}

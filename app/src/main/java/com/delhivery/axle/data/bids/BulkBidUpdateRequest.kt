package com.delhivery.axle.data.bids
import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName

data class BulkBidUpdateRequest(
    @SerializedName("bidding_type")var biddingType: String="PMT",
    @SerializedName("originator")var originator: String,
    @SerializedName("supplier_id")var supplierId: String,
    @SerializedName("unallocated_load")var unallocatedLoad: Double,
    @SerializedName("action")var action: String="bid_update",
    @SerializedName("transaction_id")var transactionId: String,
    @SerializedName("vehicle_data")var vehicleData: List<VehicleBidData>
): BaseKeyTypeModel<String>() {
    override fun key() =biddingType+":"+transactionId


}

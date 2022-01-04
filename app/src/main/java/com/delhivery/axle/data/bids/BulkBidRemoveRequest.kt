package com.delhivery.axle.data.bids

import com.google.gson.annotations.SerializedName

data class BulkBidRemoveRequest(
        @SerializedName("bidding_type")var biddingType: String="PMT",
        @SerializedName("originator")var originator: String,
        @SerializedName("supplier_id")var supplierId: String,
        @SerializedName("unallocated_load")var unallocatedLoad: Double,
        @SerializedName("action")var action: String="bid_update",
        @SerializedName("sub_action")var subAction: String?,
        @SerializedName("transaction_id")var transactionId: String,
        @SerializedName("bid_id") var bid_ids:List<String>
)

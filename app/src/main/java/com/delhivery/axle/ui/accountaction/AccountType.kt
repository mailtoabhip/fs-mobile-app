package com.delhivery.axle.ui.accountaction

import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.ui.bids.BidType

enum class AccountType(
        val typeId: Int,
val title: String
) {
    Unknown(-1, "na"),
    Both(0,  "both"),
    PostTruck(1,  "post_truck"),
    PostLoad(2,  "post_load");

    companion object {
        /**
         * Get [AccountType] by type id
         */
        fun byTypeId(typeId: Int) = AccountType.values().filter { it.typeId == typeId }.firstOrNull() ?: Unknown
    }
}
package com.delhivery.axle.ui.accountrole

import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.ui.bids.BidType

enum class AccountRole(
        val typeId: Int,
val title: String
) {
    Unknown(-1, "na"),
    Shipper(0,  "shipper"),
    Transporter(1,  "transporter"),
    Broker(2,  "broker"),
    FleetOwner(3,  "fleet_owner");

    companion object {
        /**
         * Get [AccountRole] by type id
         */
        fun byTypeId(typeId: Int) = AccountRole.values().filter { it.typeId == typeId }.firstOrNull() ?: Unknown
    }
}
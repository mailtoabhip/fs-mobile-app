package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

class HomeTrucksTimeoutItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeTrucksTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomeTrucksTimeOutAction = "time_out"
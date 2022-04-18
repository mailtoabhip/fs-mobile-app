package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTrucksWarningItemData (
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeTrucksWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeTrucksWarningAction_NoTrucks = "no_trucks"
package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

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
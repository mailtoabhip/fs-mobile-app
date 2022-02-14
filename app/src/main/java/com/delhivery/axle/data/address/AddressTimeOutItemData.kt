package com.delhivery.axle.data.address

import com.delhivery.axle.data.BaseKeyTypeModel


data class AddressTimeOutItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = AddressTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val AddressTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val AddressTimeOutAction = "time_out"
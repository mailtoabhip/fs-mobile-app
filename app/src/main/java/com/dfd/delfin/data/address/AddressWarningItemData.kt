package com.dfd.delfin.data.address

import com.dfd.delfin.data.BaseKeyTypeModel


data class AddressWarningItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = AddressWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val AddressWarningItemDataKeyPrefix = "warning_"

/* actions */
const val AddressWarningAction_NoResult = "no_results"
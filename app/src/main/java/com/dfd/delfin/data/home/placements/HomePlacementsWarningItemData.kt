package com.dfd.delfin.data.home.placements

import com.dfd.delfin.data.BaseKeyTypeModel


data class HomePlacementsWarningItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomePlacementsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomePlacementsWarningAction_NoLoads = "no_contracts"

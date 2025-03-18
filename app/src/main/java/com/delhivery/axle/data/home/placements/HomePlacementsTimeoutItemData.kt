package com.delhivery.axle.data.home.placements

import com.delhivery.axle.data.BaseKeyTypeModel


data class HomePlacementsTimeoutItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementsTimeoutItemDataKey
}

private const val HomePlacementsTimeoutItemDataKey = "timeout"
const val HomePlacementsTimeoutItemAction = "timeout_action"
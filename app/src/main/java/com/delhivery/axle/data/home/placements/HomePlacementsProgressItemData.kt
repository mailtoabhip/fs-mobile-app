package com.delhivery.axle.data.home.placements

import com.delhivery.axle.data.BaseKeyTypeModel


data class HomePlacementsProgressItemData(
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementsProgressItemDataKey
}

private const val HomePlacementsProgressItemDataKey = "progress"
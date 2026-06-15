package com.dfd.delfin.data.home.placements

import com.dfd.delfin.data.BaseKeyTypeModel


data class HomePlacementsProgressItemData(
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementsProgressItemDataKey
}

private const val HomePlacementsProgressItemDataKey = "progress"
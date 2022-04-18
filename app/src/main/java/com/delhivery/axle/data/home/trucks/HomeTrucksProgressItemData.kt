package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTrucksProgressItemData (
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksProgressItemDataKey
}

private const val HomeTrucksProgressItemDataKey = "progress"
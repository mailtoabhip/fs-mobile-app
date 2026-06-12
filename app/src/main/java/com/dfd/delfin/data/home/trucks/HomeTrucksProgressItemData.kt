package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeTrucksProgressItemData (
    val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksProgressItemDataKey
}

private const val HomeTrucksProgressItemDataKey = "progress"
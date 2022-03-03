package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTrucksSearchItemData (
    val query: String? = null
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksSearchItemDataKey
}

/* unique key for diff */
private const val HomeTrucksSearchItemDataKey = "search"

/* action id */
const val HomeTrucksSearchAction_Search = "search"
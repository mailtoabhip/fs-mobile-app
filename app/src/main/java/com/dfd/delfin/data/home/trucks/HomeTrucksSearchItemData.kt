package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeTrucksSearchItemData (
    val query: String? = null
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksSearchItemDataKey
}

/* unique key for diff */
private const val HomeTrucksSearchItemDataKey = "search"

/* action id */
const val HomeTrucksSearchAction_Search = "search"
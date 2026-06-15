package com.dfd.delfin.ui.home.fragments.contracts

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeContractsSearchItemData(
    val query: String? = null
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeContractsSearchItemDataKey
}

/* unique key for diff */
private const val HomeContractsSearchItemDataKey = "search"

/* action id */
const val HomeContractsSearchAction_Search = "search"
const val HomeContractsFilterInfo = "filter_info"
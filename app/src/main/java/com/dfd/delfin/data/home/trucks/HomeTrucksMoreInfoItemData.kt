package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

class HomeTrucksMoreInfoItemData (
    val editRouteString: String = ""
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksMoreInfoItemDataKey
}

/* unique key for diff */
const val HomeTrucksMoreInfoItemDataKey = "more_info"

/* actions */

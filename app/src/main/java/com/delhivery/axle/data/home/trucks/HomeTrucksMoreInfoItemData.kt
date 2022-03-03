package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

class HomeTrucksMoreInfoItemData (
    val editRouteString: String = ""
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksMoreInfoItemDataKey
}

/* unique key for diff */
const val HomeTrucksMoreInfoItemDataKey = "more_info"

/* actions */

package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

class HomeTrucksInfoItemData (
    val count : Int
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksInfoItemDataKey
}

/* unique key for diff */
const val HomeTrucksInfoItemDataKey = "info"

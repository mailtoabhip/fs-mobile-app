package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

class HomeTrucksInfoItemData (
    val count : Int
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksInfoItemDataKey
}

/* unique key for diff */
const val HomeTrucksInfoItemDataKey = "info"

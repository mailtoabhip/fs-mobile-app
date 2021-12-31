package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeTrucksFilterItemData(
    var actionLabel: Boolean
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeTrucksFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeTrucksFilterAction = "filter"
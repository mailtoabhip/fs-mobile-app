package com.dfd.delfin.data.home.placements

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomePlacementNoDelayItemData(val title: String="", val status:String=""
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementNoDelayItemDataKey
}

private const val HomePlacementNoDelayItemDataKey = "nodelay"


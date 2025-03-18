package com.delhivery.axle.data.home.placements

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomePlacementNoDelayItemData(val title: String="", val status:String=""
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementNoDelayItemDataKey
}

private const val HomePlacementNoDelayItemDataKey = "nodelay"


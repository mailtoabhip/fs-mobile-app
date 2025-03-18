package com.delhivery.axle.data.home.placements

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomePlacementsTypeItemData(val placementType: String = ""
) : BaseKeyTypeModel<String>() {
    override fun key() = HOME_PLACEMENT_TYPE_ITEM_DAT_KEY
}

private const val HOME_PLACEMENT_TYPE_ITEM_DAT_KEY = "placementType"
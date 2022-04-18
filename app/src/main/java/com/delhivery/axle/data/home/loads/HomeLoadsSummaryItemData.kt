package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsSummaryItemData(
    val count : Int
):BaseKeyTypeModel<String>() {
    override fun key()= HomeLoadsSummaryItemDataKey
}
private const val HomeLoadsSummaryItemDataKey = "summary"

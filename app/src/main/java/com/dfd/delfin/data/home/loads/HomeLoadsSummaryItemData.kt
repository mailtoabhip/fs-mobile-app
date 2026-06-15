package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsSummaryItemData(
    val count : Int
):BaseKeyTypeModel<String>() {
    override fun key()= HomeLoadsSummaryItemDataKey
}
private const val HomeLoadsSummaryItemDataKey = "summary"

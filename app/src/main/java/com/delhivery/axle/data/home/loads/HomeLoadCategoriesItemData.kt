package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadCategoriesItemData(
    val textDesc: String = ""):
    BaseKeyTypeModel<String>() {
    override fun key()= HomeLoadCategoriesItemDataKey
}
private const val HomeLoadCategoriesItemDataKey = "loadCategories"

package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadCategoriesItemData(
    val textDesc: String = ""):
    BaseKeyTypeModel<String>() {
    override fun key()= HomeLoadCategoriesItemDataKey
}
private const val HomeLoadCategoriesItemDataKey = "loadCategories"

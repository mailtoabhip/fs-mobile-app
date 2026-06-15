package com.dfd.delfin.data.home.placements
import com.dfd.delfin.data.BaseKeyTypeModel

data class HomePlacementsDurationItemData(
    val duration: String = ""
) : BaseKeyTypeModel<String>() {
    override fun key() = duration
}

private const val HomePlacementsDurationItemDataKey = "duration"

package com.delhivery.axle.data.home.placements
import com.delhivery.axle.data.BaseKeyTypeModel

data class HomePlacementsDurationItemData(
    val duration: String = ""
) : BaseKeyTypeModel<String>() {
    override fun key() = duration
}

private const val HomePlacementsDurationItemDataKey = "duration"

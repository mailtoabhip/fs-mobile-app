package com.delhivery.axle.data.home.trucks

import com.delhivery.axle.data.BaseKeyTypeModel

class HomeTrucksPriorityItemData(
    val showing: Boolean = true
): BaseKeyTypeModel<String>() {
    override fun key() =HomeTruckPriorityItemDataKey
}

private const val HomeTruckPriorityItemDataKey = "priority_truck"

//actions
const val HomeTrucksPriorityAction = "trucks_priority"
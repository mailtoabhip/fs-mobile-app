package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsTruckPriorityAccessItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsTruckPriorityItemDataKey
}

private const val HomeLoadsTruckPriorityItemDataKey = "priority"

//actions
const val HomeLoadsPriorityAction = "click_priority"


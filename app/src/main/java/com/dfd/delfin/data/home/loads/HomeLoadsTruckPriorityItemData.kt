package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsTruckPriorityAccessItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsTruckPriorityItemDataKey
}

private const val HomeLoadsTruckPriorityItemDataKey = "priority"

//actions
const val HomeLoadsPriorityAction = "click_priority"


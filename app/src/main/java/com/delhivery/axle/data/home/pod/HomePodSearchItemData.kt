package com.delhivery.axle.data.home.pod

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomePodSearchItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomePodSearchItemDataKey
}

/* unique key for diff */
private const val HomePodSearchItemDataKey = "search"

/* action id */
const val HomePodSearchAction_Search = "search"
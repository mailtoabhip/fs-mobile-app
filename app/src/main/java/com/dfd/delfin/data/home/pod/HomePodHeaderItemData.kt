package com.dfd.delfin.data.home.pod

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomePodHeaderItemData(
  val myBids: Int = -1
) : BaseKeyTypeModel<String>() {
  override fun key() = HomePodHeaderItemDataKey
}

/* unique key for diff */
const val HomePodHeaderItemDataKey = "header"

/* actions */
const val HomePodHeaderAction_Epod = "epod"
const val HomePodHeaderAction_Physical = "physical"
const val HomePodHeaderAction_Dispactched = "dispatch"
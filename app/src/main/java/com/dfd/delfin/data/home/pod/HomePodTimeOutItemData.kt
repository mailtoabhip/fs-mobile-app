package com.dfd.delfin.data.home.pod

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomePodTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomePodTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomePodTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomePodTimeOutAction = "time_out"
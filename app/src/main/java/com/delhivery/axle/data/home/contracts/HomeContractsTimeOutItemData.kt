package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeContractsTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val HomeContractsTimeOutAction = "time_out"
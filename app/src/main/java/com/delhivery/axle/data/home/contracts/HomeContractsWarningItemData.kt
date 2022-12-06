package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val HomeContractsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val HomeContractsWarningAction_NoLoads = "no_contracts"
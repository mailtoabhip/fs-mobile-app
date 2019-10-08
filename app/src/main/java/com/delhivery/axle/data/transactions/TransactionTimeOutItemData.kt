package com.delhivery.axle.data.transactions

import com.delhivery.axle.data.BaseKeyTypeModel

/**
 * Timeout item data for wallet transaction list
 */
data class TransactionTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val TransactionTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val TransactionTimeOutAction = "time_out"
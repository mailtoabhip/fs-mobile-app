package com.dfd.delfin.data.transactions

import com.dfd.delfin.data.BaseKeyTypeModel

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
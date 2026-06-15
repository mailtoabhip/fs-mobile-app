package com.dfd.delfin.data.transactions

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Warning item data for wallet transaction list
 */
data class TransactionWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val TransactionWarningItemDataKeyPrefix = "warning_"

/* actions */
const val TransactionWarningAction_NoTransactions = "no_transactions"
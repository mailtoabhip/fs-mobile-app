package com.dfd.delfin.data.transactions

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Progress item data for wallet transaction list
 */
data class TransactionProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionProgressItemDataKey
}

private const val TransactionProgressItemDataKey = "progress"
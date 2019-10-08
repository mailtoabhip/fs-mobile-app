package com.delhivery.axle.data.transactions

import com.delhivery.axle.data.BaseKeyTypeModel

/**
 * Progress item data for wallet transaction list
 */
data class TransactionProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionProgressItemDataKey
}

private const val TransactionProgressItemDataKey = "progress"
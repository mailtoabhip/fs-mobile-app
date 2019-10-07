package com.delhivery.axle.data.transactions

import com.delhivery.axle.api.response.WalletData
import com.delhivery.axle.data.BaseKeyTypeModel

data class TransactionHeaderItemData(
  val wallet: WalletData,
  val numTransactions: Int? = 0
) : BaseKeyTypeModel<String>() {
  override fun key() = TransactionHeaderItemDataKey

  fun numTransactions() = when (numTransactions) {
    0, null -> "Transaction History (0)"
    else -> "Transaction History ($numTransactions)"
  }
}

/* unique key for diff */
const val TransactionHeaderItemDataKey = "header"
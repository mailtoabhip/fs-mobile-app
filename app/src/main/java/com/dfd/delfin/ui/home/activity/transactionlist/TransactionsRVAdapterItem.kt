package com.dfd.delfin.ui.home.activity.transactionlist

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.transactions.TransactionHeaderItemData
import com.dfd.delfin.data.transactions.TransactionProgressItemData
import com.dfd.delfin.data.transactions.TransactionTimeOutItemData
import com.dfd.delfin.data.transactions.TransactionWarningItemData
import com.dfd.delfin.data.transactions.TransactionsItemData
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Header
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Transaction
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Warning

/**
 * RV item type for [TransactionsRVAdapter]
 */
enum class TransactionsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Transaction(1),
  Progress(2),
  Warning(3),
  Timeout(4);

  companion object {
    /**
     * Get [TransactionsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base home trips type adapter item
 */
abstract class BaseTransactionsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: TransactionsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Trip item
 */
class TransactionDataItem(data: TransactionsItemData) :
    BaseTransactionsRVAdapterItem<TransactionsItemData>(Transaction, data)

/**
 * Inline progress item
 */
class TransactionsProgressItem(data: TransactionProgressItemData = TransactionProgressItemData()) :
    BaseTransactionsRVAdapterItem<TransactionProgressItemData>(Progress, data)

/**
 * Trip header items
 */
class TransactionHeaderItem(data: TransactionHeaderItemData) :
    BaseTransactionsRVAdapterItem<TransactionHeaderItemData>(Header, data)

/**
 * Warning/action item
 */
class TransactionWarningItem(data: TransactionWarningItemData) :
    BaseTransactionsRVAdapterItem<TransactionWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class TransactionTimeoutItem(data: TransactionTimeOutItemData) :
    BaseTransactionsRVAdapterItem<TransactionTimeOutItemData>(Timeout, data)
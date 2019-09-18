package com.delhivery.axle.ui.home.activity.transactionlist

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.transactions.TransactionHeaderItemData
import com.delhivery.axle.data.transactions.TransactionProgressItemData
import com.delhivery.axle.data.transactions.TransactionTimeOutItemData
import com.delhivery.axle.data.transactions.TransactionWarningItemData
import com.delhivery.axle.data.transactions.TransactionsItemData
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Transaction
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Warning

enum class TransactionsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Transaction(1),
  Progress(2),
  Warning(3),
  Timeout(4);

  companion object {
    /**
     * Get [HomeTripsRVAdapterItemType] by typeId
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
class TransactionHeaderItem(
  data: TransactionHeaderItemData = TransactionHeaderItemData()
) : BaseTransactionsRVAdapterItem<TransactionHeaderItemData>(Header, data)

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
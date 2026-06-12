package com.dfd.delfin.ui.home.activity.transactionlist

import com.dfd.delfin.data.transactions.TransactionAction_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Transaction

/**
 * Adapter interface for [TransactionsActivity]
 */
interface TransactionsRVAdapterInterface : ItemClickListener<BaseTransactionsRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseTransactionsRVAdapterItem<*>) {
    if (item.type == Transaction) {
      handleAction(TransactionAction_ViewDetails, item)
    }
  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseTransactionsRVAdapterItem<*>
  )
}
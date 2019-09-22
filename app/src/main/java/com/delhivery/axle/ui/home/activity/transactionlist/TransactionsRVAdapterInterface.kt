package com.delhivery.axle.ui.home.activity.transactionlist

import com.delhivery.axle.data.transactions.TransactionAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Transaction

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
package com.delhivery.axle.ui.home.activity.transactionlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewTransactionHeaderItemBinding
import com.delhivery.axle.databinding.ViewTransactionItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsRVAdapterItemType.Warning

class TransactionsRVAdapter(private val _interface: TransactionsRVAdapterInterface) :
    BaseDataRVAdapter<BaseTransactionsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (TransactionsRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewTransactionHeaderItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewTransactionItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewTransactionHeaderItemBinding -> TransactionHeaderItemVH(binding)
//    is ViewHomeTripsProgressItemBinding -> TransactionsProgressItemVH(binding)
    is ViewWarningItemBinding -> TransactionWarningItemVH(binding)
    is ViewTimeOutItemBinding -> TransactionTimeOutItemVH(binding)
    else -> TransactionsItemVH(binding as ViewTransactionItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseTransactionsRVAdapterItem<*>
  ) {
    when (holder) {
      is TransactionHeaderItemVH -> holder.bind(item as TransactionHeaderItem, _interface)
      is TransactionsItemVH -> holder.bind(item as TransactionDataItem, _interface)
//      is HomeTripsProgressItemVH -> holder.bind(item as HomeTripsProgressItem, _interface)
//      is HomeTripsHeaderItemVH -> holder.bind(item as HomeTripsHeaderItem, _interface)
      is TransactionWarningItemVH -> holder.bind(item as TransactionWarningItem, _interface)
      is TransactionTimeOutItemVH -> holder.bind(item as TransactionTimeoutItem, _interface)
    }
  }

  /**
   * Reset to empty state with search bar
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseTransactionsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      //      add(Pair(HomeTripsHeaderItem(), AddUpdate))
//      add(Pair(HomeTripsProgressItem(), AddUpdate))
//      items.filter { it.type == TripItem || it.type == Warning || it.type == Timeout }
//          .map { Pair(it, Remove) }
//          .let {
//            addAll(it)
//          }
    }
        .let {
          operation(it)
        }
  }

}
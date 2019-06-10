package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.orion.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner

class SearchLoadsRVAdapter(private val _interface: SearchLoadsRVAdapterInterface) :
    BaseDataRVAdapter<BaseSearchLoadsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchResultsRVAdapterItemType.byTypeId(viewType)) {
    Request -> ViewHomeLoadsRequestItemBinding.inflate(inflater, parent, false)
    SearchSpinner -> ViewHomeBidsSearchSpinnerItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsRequestItemBinding -> SearchLoadsRequestItemVH(binding)
    is ViewHomeBidsSearchSpinnerItemBinding -> SearchLoadsSearchSpinnerItemVH(binding)
    else -> SearchLoadsRequestItemVH(binding as ViewHomeLoadsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchLoadsRequestItemVH -> holder.bind(item as SearchLoadsRequestItem, _interface)
    }
  }

  /**
   * Remove all transactions
   */
  fun removeAllTransactions() {
    items.filter { it.type == Request }
        .map { Pair(it, Remove) }
        .let {
          operation(it)
        }
  }

}
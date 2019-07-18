package com.delhivery.axle.ui.searchload.fragments.searchresults

import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner

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
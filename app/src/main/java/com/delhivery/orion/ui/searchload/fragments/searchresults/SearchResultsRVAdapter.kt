package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Search
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner

class SearchResultsRVAdapter(private val _interface: SearchResultsRVAdapterInterface) :
    BaseDataRVAdapter<BaseSearchResultsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchResultsRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewHomeBidsSearchItemBinding.inflate(
        inflater, parent,
        false
    )
    Request -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
    SearchSpinner -> ViewHomeBidsSearchSpinnerItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeBidsSearchItemBinding -> SearchResultsSearchItemVH(binding)
    is ViewHomeBidsRequestItemBinding -> SearchResultsRequestItemVH(binding)
    is ViewHomeBidsSearchSpinnerItemBinding -> SearchResultsSearchSpinnerItemVH(binding)
    else -> SearchResultsRequestItemVH(binding as ViewHomeBidsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchResultsRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchResultsSearchItemVH -> holder.bind(item as HomeBidsSearchItem, _interface)
      is SearchResultsRequestItemVH -> holder.bind(item as HomeBidsRequestItem, _interface)
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
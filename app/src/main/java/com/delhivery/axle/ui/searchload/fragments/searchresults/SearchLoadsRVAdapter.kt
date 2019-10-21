package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsRequestItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Warning

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
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeLoadsRequestItemBinding -> SearchLoadsRequestItemVH(binding)
    is ViewHomeBidsSearchSpinnerItemBinding -> SearchLoadsSearchSpinnerItemVH(binding)
    is ViewWarningItemBinding -> SearchLoadsWarningItemVH(binding)
    else -> SearchLoadsRequestItemVH(binding as ViewHomeLoadsRequestItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchLoadsRequestItemVH -> holder.bind(item as SearchLoadsRequestItem, _interface)
      is SearchLoadsSearchSpinnerItemVH -> holder.bind(
          item as SearchLoadsSearchSpinnerItem, _interface
      )
      is SearchLoadsWarningItemVH -> holder.bind(item as SearchLoadsWarningItem, _interface)
    }
  }

}
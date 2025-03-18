package com.delhivery.axle.ui.searchcitystate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeTripsRequestItemBinding
import com.delhivery.axle.databinding.ViewSearchCityItemBinding
import com.delhivery.axle.databinding.ViewSearchCityProgressItemBinding
import com.delhivery.axle.databinding.ViewSearchCityStateItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

class PopularOriginLocationAdapter (private val _interface: SearchCityStateRVAdapterInterface) :
  BaseDataRVAdapter<BaseCityStateRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
  ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchCityStateRvAdapterItemType.byTypeId(viewType)) {
    SearchCityStateRvAdapterItemType.CityItem -> ViewSearchCityItemBinding.inflate(
      inflater,
      parent,
      false
    )
    SearchCityStateRvAdapterItemType.Progress -> ViewSearchCityProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewSearchCityItemBinding -> SearchOriginCityDataItemVH(binding)
    is ViewSearchCityProgressItemBinding -> SearchProgressItemVH(binding)
    else -> SearchDataItemVH(binding as ViewSearchCityStateItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseCityStateRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchOriginCityDataItemVH -> holder.bind(item as SearchDataItem, _interface)
    }
  }

}
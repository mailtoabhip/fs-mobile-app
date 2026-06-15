package com.dfd.delfin.ui.searchcitystate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

class SelectedCityStateRvAdapter(private val _interface: SearchCityStateRVAdapterInterface) :
    BaseDataRVAdapter<BaseCityStateRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (SearchCityStateRvAdapterItemType.byTypeId(viewType)) {
        SearchCityStateRvAdapterItemType.CityItem -> ViewSelectedCitiesItemBinding.inflate(
            inflater,
            parent,
            false
        )
        else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewSelectedCitiesItemBinding -> SelectedDataItemVH(binding)
        else -> SelectedDataItemVH(binding as ViewSelectedCitiesItemBinding)
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseCityStateRVAdapterItem<*>
    ) {
        when (holder) {
            is SelectedDataItemVH -> holder.bind(item as SearchDataItem, _interface)
        }
    }

}
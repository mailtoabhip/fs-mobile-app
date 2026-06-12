package com.dfd.delfin.ui.searchcitystate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewHomeTripsRequestItemBinding
import com.dfd.delfin.databinding.ViewSearchCityProgressItemBinding
import com.dfd.delfin.databinding.ViewSearchCityStateItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

class PopularCitiesAdapter (private val _interface: SearchCityStateRVAdapterInterface) :
    BaseDataRVAdapter<BaseCityStateRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (SearchCityStateRvAdapterItemType.byTypeId(viewType)) {
        SearchCityStateRvAdapterItemType.CityItem -> ViewSearchCityStateItemBinding.inflate(
            inflater,
            parent,
            false
        )
        SearchCityStateRvAdapterItemType.Progress -> ViewSearchCityProgressItemBinding.inflate(inflater, parent, false)
        else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewSearchCityStateItemBinding -> SearchDataItemVH(binding)
        is ViewSearchCityProgressItemBinding -> SearchProgressItemVH(binding)
        else -> SearchDataItemVH(binding as ViewSearchCityStateItemBinding)
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseCityStateRVAdapterItem<*>
    ) {
        when (holder) {
            is SearchDataItemVH -> holder.bind(item as SearchDataItem, _interface)
        }
    }

}
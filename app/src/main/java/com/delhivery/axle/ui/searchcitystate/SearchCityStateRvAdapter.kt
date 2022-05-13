package com.delhivery.axle.ui.searchcitystate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType

class SearchCityStateRvAdapter(private val _interface: SearchCityStateRVAdapterInterface) :
    BaseDataRVAdapter<BaseCityStateRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (SearchCityStateRvAdapterItemType.byTypeId(viewType)) {
        SearchCityStateRvAdapterItemType.CityItem -> ViewSearchCityStateItemBinding.inflate(inflater, parent, false)
        SearchCityStateRvAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
        SearchCityStateRvAdapterItemType.Progress -> ViewSearchCityProgressItemBinding.inflate(inflater, parent, false)
        SearchCityStateRvAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewSearchCityStateItemBinding -> SearchDataItemVH(binding)
        is ViewWarningItemBinding -> SearchWarningItemVH(binding)
        is ViewTimeOutItemBinding -> SearchTimeOutItemVH(binding)
        is ViewSearchCityProgressItemBinding -> SearchProgressItemVH(binding)
        else -> SearchDataItemVH(binding as ViewSearchCityStateItemBinding)
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseCityStateRVAdapterItem<*>
    ) {
        when (holder) {
            is SearchDataItemVH -> holder.bind(item as SearchDataItem, _interface)
            is SearchWarningItemVH -> holder.bind(item as SearchWarningItem, _interface)
            is SearchTimeOutItemVH -> holder.bind(item as SearchTimeoutItem, _interface)
        }
    }

    /**
     * Reset all data, remove all errors/transactions
     */
    fun resetStaticData() {
        mutableListOf<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            items.filter {
                it.type == SearchCityStateRvAdapterItemType.CityItem || it.type == SearchCityStateRvAdapterItemType.Warning || it.type == SearchCityStateRvAdapterItemType.Timeout
            }
                .map { Pair(it, DataRVAdapterOperationType.Remove) }
                .let {
                    addAll(it)
                }
        }
            .let {
                operation(it)
            }
    }

}
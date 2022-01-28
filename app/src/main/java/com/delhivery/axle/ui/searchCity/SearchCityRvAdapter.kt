package com.delhivery.axle.ui.searchCity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType

class SearchCityRvAdapter(private val _interface: SearchCityRVAdapterInterface) :
    BaseDataRVAdapter<BaseCityRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
    ) {

        override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

        override fun getBinding(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
        ) = when (SearchCityRvAdapterItemType.byTypeId(viewType)) {
            SearchCityRvAdapterItemType.CityItem -> ViewSearchCityItemBinding.inflate(inflater, parent, false)
            SearchCityRvAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
            SearchCityRvAdapterItemType.Progress -> ViewSearchCityProgressItemBinding.inflate(inflater, parent, false)
            SearchCityRvAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
            else -> ViewHomeTripsRequestItemBinding.inflate(inflater, parent, false)
        }

        override fun createVH(binding: ViewDataBinding) = when (binding) {
            is ViewSearchCityItemBinding -> SearchDataItemVH(binding)
            is ViewWarningItemBinding -> SearchWarningItemVH(binding)
            is ViewTimeOutItemBinding -> SearchTimeOutItemVH(binding)
            is ViewSearchCityProgressItemBinding -> SearchProgressItemVH(binding)
            else -> SearchDataItemVH(binding as ViewSearchCityItemBinding)
        }

        override fun bindVH(
            holder: BaseViewHolder<*>,
            item: BaseCityRVAdapterItem<*>
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
            mutableListOf<Pair<BaseCityRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                items.filter {
                    it.type == SearchCityRvAdapterItemType.CityItem || it.type == SearchCityRvAdapterItemType.Warning || it.type == SearchCityRvAdapterItemType.Timeout
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
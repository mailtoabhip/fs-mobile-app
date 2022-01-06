package com.delhivery.axle.ui.home.fragments.trucks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksRVAdapterItemType

class HomeTrucksRVAdapter(private val _interface : HomeTrucksRVAdapterInterface):
    BaseDataRVAdapter<BaseHomeTrucksRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
) {

    override fun getItemViewType(position: Int) = items[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (HomeTrucksRVAdapterItemType.byTypeId(viewType)) {
        HomeTrucksRVAdapterItemType.Search -> ViewHomeTrucksSearchItemBinding.inflate(inflater, parent, false)
        HomeTrucksRVAdapterItemType.Progress -> ViewHomeTrucksProgressItemBinding.inflate(inflater, parent, false)
        HomeTrucksRVAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
        HomeTrucksRVAdapterItemType.Info -> ViewHomeTrucksInfoItemBinding.inflate(inflater, parent, false)
        HomeTrucksRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        HomeTrucksRVAdapterItemType.Filters -> ViewHomeTrucksFilterItemBinding.inflate(inflater, parent, false)
        else -> ViewHomeTrucksRequestItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when(binding) {
        is ViewHomeTrucksSearchItemBinding -> HomeTrucksSearchItemVH(binding)
        is ViewHomeTrucksProgressItemBinding -> HomeTrucksProgressItemVH(binding)
        is ViewWarningItemBinding -> HomeTrucksWarningItemVH(binding)
        is ViewHomeTrucksInfoItemBinding -> HomeTrucksInfoItemVH(binding)
        is ViewTimeOutItemBinding -> HomeTrucksTimeOutItemVH(binding)
        is ViewHomeTrucksFilterItemBinding -> HomeTrucksFilterItemVH(binding)
        else -> HomeTrucksRequestItemVH(binding as ViewHomeTrucksRequestItemBinding)
    }
    

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseHomeTrucksRVAdapterItem<*>) {
        when(holder) {
            is HomeTrucksRequestItemVH -> holder.bind(item as HomeTrucksRequestItem, _interface)
            is HomeTrucksProgressItemVH -> holder.bind(item as HomeTrucksProgressItem, _interface)
            is HomeTrucksSearchItemVH -> holder.bind(item as HomeTrucksSearchItem, _interface)
            is HomeTrucksWarningItemVH -> holder.bind(item as HomeTrucksWarningItem, _interface)
            is HomeTrucksTimeOutItemVH -> holder.bind(item as HomeTrucksTimeoutItem, _interface)
            is HomeTrucksInfoItemVH -> holder.bind(item as HomeTrucksInfoItem, _interface)
            is HomeTrucksFilterItemVH -> holder.bind(item as HomeTrucksFilterItem, _interface)
        }

    }

    /**
     * Reset all data, remove all errors/transactions
     */
    fun resetStaticData() {
        mutableListOf<Pair<BaseHomeTrucksRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(HomeTrucksFilterItem(), DataRVAdapterOperationType.AddUpdate))
            items.filter {
                it.type == HomeTrucksRVAdapterItemType.Request || it.type == HomeTrucksRVAdapterItemType.Warning ||
                it.type == HomeTrucksRVAdapterItemType.Timeout || it.type == HomeTrucksRVAdapterItemType.Info || it.type == HomeTrucksRVAdapterItemType.MoreInfo
                || it.type == HomeTrucksRVAdapterItemType.Search
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
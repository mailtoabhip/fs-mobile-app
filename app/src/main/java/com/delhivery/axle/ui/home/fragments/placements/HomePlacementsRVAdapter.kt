package com.delhivery.axle.ui.home.fragments.placements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewPlacementsDurationsBinding
import com.delhivery.axle.databinding.ViewPlacementsFiltersBinding
import com.delhivery.axle.databinding.ViewPlacementsNoDelayBinding
import com.delhivery.axle.databinding.ViewPlacementsTypeBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewVehiclePlacementBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType


class HomePlacementsRVAdapter (private val _interface: HomePlacementsRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomePlacementsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int) = items[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (HomePlacementsRVAdapterItemType.byTypeId(viewType)) {
        HomePlacementsRVAdapterItemType.PlacementItem ->ViewVehiclePlacementBinding.inflate(inflater,parent,false)
        HomePlacementsRVAdapterItemType.Progress -> ViewHomeContractsProgressItemBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.Filters -> ViewPlacementsFiltersBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.NonDelay -> ViewPlacementsNoDelayBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.Header -> ViewPlacementsTypeBinding.inflate(inflater, parent, false)
        HomePlacementsRVAdapterItemType.Duration -> ViewPlacementsDurationsBinding.inflate(inflater, parent, false)
        else -> ViewWarningItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewVehiclePlacementBinding -> HomeVehiclePlacementsRequestItemVH(binding)
        is ViewPlacementsTypeBinding -> HomePlacementsTypeItemVH(binding)
        is ViewHomeContractsProgressItemBinding -> HomePlacementsProgressItemVH(binding)
        is ViewWarningItemBinding -> HomePlacementsWarningItemVH(binding)
        is ViewTimeOutItemBinding -> HomePlacementsTimeOutItemVH(binding)
        is ViewPlacementsFiltersBinding -> HomePlacementsFilterItemVH(binding)
        is ViewPlacementsDurationsBinding -> HomePlacementsDurationItemVH(binding)
        is ViewPlacementsNoDelayBinding -> HomePlacementsNoDelayItemVH(binding)
        else -> HomePlacementsWarningItemVH(binding as ViewWarningItemBinding )
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseHomePlacementsRVAdapterItem<*>
    ) {
        when (holder) {
            is HomeVehiclePlacementsRequestItemVH -> holder.bind(item as HomeVehiclePlacementsRequestItem, _interface)
            is HomePlacementsTypeItemVH -> holder.bind(item as HomePlacementsTypeItem, _interface)
            is HomePlacementsProgressItemVH -> holder.bind(item as HomePlacementsProgressItem, _interface)
            is HomePlacementsWarningItemVH -> holder.bind(item as HomePlacementsWarningItem, _interface)
            is HomePlacementsTimeOutItemVH -> holder.bind(item as HomePlacementsTimeoutItem, _interface)
            is HomePlacementsFilterItemVH -> holder.bind(item as HomePlacementsFilterItem, _interface)
            is HomePlacementsDurationItemVH -> holder.bind(item as HomePlacementsDurationItem, _interface)
            is HomePlacementsNoDelayItemVH -> holder.bind(item as HomePlacementsNoDelayItem, _interface)

        }
    }

    /**
     * Reset all data, remove all errors/transactions
     */
    fun resetStaticData() {
        mutableListOf<Pair<BaseHomePlacementsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(HomePlacementsProgressItem(), DataRVAdapterOperationType.AddUpdate))
            items.filter {
                it.type == HomePlacementsRVAdapterItemType.Header  ||  it.type == HomePlacementsRVAdapterItemType.Duration || it.type == HomePlacementsRVAdapterItemType.NonDelay||  it.type == HomePlacementsRVAdapterItemType.PlacementItem||
                          it.type == HomePlacementsRVAdapterItemType.Filters ||   it.type == HomePlacementsRVAdapterItemType.Timeout ||  it.type == HomePlacementsRVAdapterItemType.Warning
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

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
    }
}
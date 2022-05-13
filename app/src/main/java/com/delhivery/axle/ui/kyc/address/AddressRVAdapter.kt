package com.delhivery.axle.ui.kyc.address

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType


class AddressRVAdapter(private val _interface: AddressRVAdapterInterface) :
    BaseDataRVAdapter<BaseAddressRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = when (AddressRVAdapterItemType.byTypeId(viewType)) {
        AddressRVAdapterItemType.AddressItem -> ViewAddressRequestItemBinding.inflate(inflater, parent, false)
        AddressRVAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
//        AddressRVAdapterItemType.Progress -> ViewGstProgressItemBinding.inflate(inflater, parent, false)
        AddressRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
        else -> ViewAddressRequestItemBinding.inflate(inflater, parent, false)
    }

    override fun createVH(binding: ViewDataBinding) = when (binding) {
        is ViewAddressRequestItemBinding -> AddressDataItemVH(binding)
//        is ViewGstProgressItemBinding -> GstProgressItemVH(binding)
        is ViewWarningItemBinding -> AddressWarningItemVH(binding)
        is ViewTimeOutItemBinding -> AddressTimeOutItemVH(binding)
        else -> AddressDataItemVH(binding as ViewAddressRequestItemBinding)
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseAddressRVAdapterItem<*>
    ) {
        when (holder) {
            is AddressDataItemVH -> holder.bind(item as AddressDataItem, _interface)
            is AddressWarningItemVH -> holder.bind(item as AddressWarningItem, _interface)
            is AddressTimeOutItemVH -> holder.bind(item as AddressTimeoutItem, _interface)
        }
    }


    fun refresh() {
        mutableListOf<Pair<BaseAddressRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
//            add(Pair(AddressProgressItem(), DataRVAdapterOperationType.AddUpdate))
            items.filter {
                it.type == AddressRVAdapterItemType.AddressItem|| it.type == AddressRVAdapterItemType.Warning ||
                        it.type == AddressRVAdapterItemType.Timeout
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
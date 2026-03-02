package com.delhivery.axle.ui.loadwallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ItemWalletHistoryBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType

/**
 * RV Adapter for [LoadWalletActivity]
 */
class LoadWalletRVAdapter(private val _interface: LoadWalletRVAdapterInterface) :
    BaseDataRVAdapter<BaseLoadWalletRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

    override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewDataBinding = ItemWalletHistoryBinding.inflate(inflater, parent, false)

    override fun createVH(binding: ViewDataBinding): BaseViewHolder<*> = when (binding) {
        is ItemWalletHistoryBinding -> WalletHistoryItemVH(binding)
        else -> WalletHistoryItemVH(binding as ItemWalletHistoryBinding)
    }

    override fun bindVH(
        holder: BaseViewHolder<*>,
        item: BaseLoadWalletRVAdapterItem<*>
    ) {
        when (holder) {
            is WalletHistoryItemVH -> holder.bind(item as WalletHistoryItem, _interface)
        }
    }

    /**
     * Reset to empty state
     */
    fun resetData() {
        mutableListOf<Pair<BaseLoadWalletRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            items.filter { it.type == LoadWalletRVAdapterItemType.HistoryItem }
                .map { Pair(it, DataRVAdapterOperationType.Remove) }
                .let { addAll(it) }
        }.let { operation(it) }
    }

    /**
     * @return current list of items
     */
    fun getData(): List<BaseLoadWalletRVAdapterItem<*>> = items.toList()
}

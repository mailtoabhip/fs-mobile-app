package com.delhivery.axle.ui.loadwallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.delhivery.axle.databinding.ItemWalletHistoryBinding
import com.delhivery.axle.ui.base.BaseViewHolder

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BaseLoadWalletRVAdapterItem<*>>() {
    override fun areItemsTheSame(
        oldItem: BaseLoadWalletRVAdapterItem<*>,
        newItem: BaseLoadWalletRVAdapterItem<*>
    ): Boolean = oldItem.key() == newItem.key()

    override fun areContentsTheSame(
        oldItem: BaseLoadWalletRVAdapterItem<*>,
        newItem: BaseLoadWalletRVAdapterItem<*>
    ): Boolean = oldItem.data == newItem.data
}

/**
 * RV Adapter for [LoadWalletActivity] — uses AsyncListDiffer for efficient updates.
 */
class LoadWalletRVAdapter(private val _interface: LoadWalletRVAdapterInterface) :
    androidx.recyclerview.widget.RecyclerView.Adapter<BaseViewHolder<*>>() {

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int) = differ.currentList[position].type.typeId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val binding = ItemWalletHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalletHistoryItemVH(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is WalletHistoryItemVH -> holder.bind(item as WalletHistoryItem, _interface)
        }
    }

    fun submitList(items: List<BaseLoadWalletRVAdapterItem<*>>, onCommitted: (() -> Unit)? = null) {
        differ.submitList(items, onCommitted)
    }

    fun updateItem(txnId: String, newStatus: String) {
        val updated = differ.currentList.map { item ->
            if (item is WalletHistoryItem && item.data.txnNumber == txnId) {
                WalletHistoryItem(item.data.copy(status = newStatus))
            } else {
                item
            }
        }
        differ.submitList(updated)
    }


    fun getData(): List<BaseLoadWalletRVAdapterItem<*>> = differ.currentList

    fun resetData() {
        differ.submitList(emptyList())
    }
}

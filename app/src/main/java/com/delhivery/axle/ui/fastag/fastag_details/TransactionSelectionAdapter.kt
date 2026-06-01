package com.delhivery.axle.ui.fastag.fastag_details

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.databinding.ItemTransactionSelectionBinding
import kotlin.math.abs

class TransactionSelectionAdapter(
    private val onItemClick: (TransactionItem) -> Unit
) : ListAdapter<TransactionItem, TransactionSelectionAdapter.ViewHolder>(DiffCallback()) {

    private var selectedTransaction: TransactionItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectedTransaction(transaction: TransactionItem) {
        val previousSelected = selectedTransaction
        selectedTransaction = transaction

        currentList.forEachIndexed { index, item ->
            if (item == previousSelected || item == transaction) {
                notifyItemChanged(index)
            }
        }
    }

    fun getSelectedTransaction(): TransactionItem? = selectedTransaction

    inner class ViewHolder(
        private val binding: ItemTransactionSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionItem) {
            binding.tvTollName.text = transaction.tollName
            binding.tvTimestamp.text = transaction.timestamp

            val amountText = transaction.amount?.let {
                if (it < 0) {
                    "-₹${abs(transaction.amount).toInt()}"
                } else {
                    "+₹${transaction.amount.toInt()}"
                }
            }
            binding.tvAmount.text = amountText

            val amountColor = transaction.amount?.let {
                if (it < 0) {
                    Color.parseColor("#111827")
                } else {
                    Color.parseColor("#16A34A")
                }
            }
            binding.tvAmount.setTextColor(amountColor?:0)

            binding.rbSelect.isChecked = transaction == selectedTransaction

            binding.root.setOnClickListener {
                onItemClick(transaction)
            }

            binding.rbSelect.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TransactionItem>() {
        override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem == newItem
        }
    }
}

package com.delhivery.axle.ui.fastag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.api.response.FastagTransaction
import com.delhivery.axle.databinding.ItemFastagTransactionBinding

class FastagTransactionAdapter : ListAdapter<FastagTransaction, FastagTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemFastagTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(private val binding: ItemFastagTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: FastagTransaction) {
            binding.tvTransactionId.text = "#${transaction.txnId}"
            
            binding.tvTimestamp.text = formatTimestamp(transaction.timestamp?:"")
            
            binding.tvTollName.text = transaction.tollName ?: transaction.transactionType
            
            val amountText = when {
                transaction.transactionType?.contains("credit", ignoreCase = true) == true ||
                        transaction.transactionType?.contains("recharge", ignoreCase = true) == true ->
                    "+₹${transaction.amount}"
                else -> "-₹${transaction.amount}"
            }
            binding.tvAmount.text = amountText
            
            val amountColor = when {
                transaction.transactionType?.contains("credit", ignoreCase = true) == true ||
                        transaction.transactionType?.contains("recharge", ignoreCase = true) == true ->
                    android.graphics.Color.parseColor("#059669") // Green for credit
                else ->
                    android.graphics.Color.parseColor("#505361")
            }
            binding.tvAmount.setTextColor(amountColor)
            
            binding.tvAvailableBalance.text = "₹${transaction.avlBalance}"
        }

        private fun formatTimestamp(timestamp: String): String {
            return com.delhivery.axle.utils.DateUtils.formatFastagTransactionDate(timestamp)
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<FastagTransaction>() {
        override fun areItemsTheSame(oldItem: FastagTransaction, newItem: FastagTransaction): Boolean {
            return oldItem.txnId == newItem.txnId
        }

        override fun areContentsTheSame(oldItem: FastagTransaction, newItem: FastagTransaction): Boolean {
            return oldItem == newItem
        }
    }
}

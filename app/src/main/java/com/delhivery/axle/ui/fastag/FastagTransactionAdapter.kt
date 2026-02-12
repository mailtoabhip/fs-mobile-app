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

            // Handle dispute status visibility and styling
            if (transaction.isDispute == true && transaction.disputeStatus?.isNotEmpty() == true) {
                binding.tvDisputeStatus.visibility = android.view.View.VISIBLE
                
                when (transaction.disputeStatus.lowercase()) {
                    "submitted" -> {
                        binding.tvDisputeStatus.text = "Dispute submitted"
                        setDisputeBackground(binding.tvDisputeStatus, "#FFF7ED")
                        binding.tvDisputeStatus.setTextColor(android.graphics.Color.parseColor("#92400E"))
                    }
                    "accepted" -> {
                        binding.tvDisputeStatus.text = "Dispute accepted"
                        setDisputeBackground(binding.tvDisputeStatus, "#D1FAE5")
                        binding.tvDisputeStatus.setTextColor(android.graphics.Color.parseColor("#065F46"))
                    }
                    "rejected" -> {
                        binding.tvDisputeStatus.text = "Dispute rejected"
                        setDisputeBackground(binding.tvDisputeStatus, "#FEE2E2")
                        binding.tvDisputeStatus.setTextColor(android.graphics.Color.parseColor("#991B1B"))
                    }
                    else -> {
                        binding.tvDisputeStatus.visibility = android.view.View.GONE
                    }
                }
            } else {
                binding.tvDisputeStatus.visibility = android.view.View.GONE
            }

            // Click listener to open transaction detail
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = android.content.Intent(context, FastagTransactionDetailActivity::class.java).apply {
                    putExtra(FastagTransactionDetailActivity.EXTRA_TXN_ID, transaction.txnId)
                    putExtra(FastagTransactionDetailActivity.EXTRA_AMOUNT, transaction.amount ?: 0.0)
                    putExtra(FastagTransactionDetailActivity.EXTRA_TOLL_NAME, transaction.tollName)
                    putExtra(FastagTransactionDetailActivity.EXTRA_TIMESTAMP, formatTimestamp(transaction.timestamp ?: ""))
                }
                context.startActivity(intent)
            }

        }

        private fun setDisputeBackground(view: android.widget.TextView, colorHex: String) {
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            drawable.cornerRadius = view.context.resources.getDimension(com.delhivery.axle.R.dimen.size_16dp)
            drawable.setColor(android.graphics.Color.parseColor(colorHex))
            view.background = drawable
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

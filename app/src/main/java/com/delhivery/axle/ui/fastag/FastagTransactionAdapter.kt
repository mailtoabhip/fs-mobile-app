package com.delhivery.axle.ui.fastag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.api.response.FastagTransaction
import com.delhivery.axle.databinding.ItemFastagTransactionBinding
import androidx.core.graphics.toColorInt

class FastagTransactionAdapter(
    private val vehicleData: com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData? = null
) : ListAdapter<FastagTransaction, FastagTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemFastagTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, vehicleData)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemFastagTransactionBinding,
        private val vehicleData: com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData?
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: FastagTransaction) {
            // Title: txn_event / tollName (e.g. "552003-Agra Toll Plaza")
            binding.tvTransactionId.text = transaction.tollName ?: ""
            
            binding.tvTimestamp.text = formatTimestamp(transaction.timestamp ?: "")
            
            // Subtitle: txnEventContext (e.g. "Toll Debit", "Account Activity")
            binding.tvTollName.text = transaction.txnEventContext ?: transaction.transactionType ?: ""
            
            // Amount with sign
            val isCredit = transaction.transactionType?.contains("credit", ignoreCase = true) == true ||
                    transaction.transactionType?.contains("recharge", ignoreCase = true) == true ||
                    transaction.transactionType?.contains("refund", ignoreCase = true) == true ||
                    transaction.txnEventContext?.contains("refund", ignoreCase = true) == true
            
            val amountText = if (isCredit) "+₹${transaction.amount?.toInt() ?: 0}" else "-₹${transaction.amount?.toInt() ?: 0}"
            binding.tvAmount.text = amountText
            
            val amountColor = if (isCredit) {
                android.graphics.Color.parseColor("#10B981") // Green for credit/refund
            } else {
                android.graphics.Color.parseColor("#0F172A")
            }
            binding.tvAmount.setTextColor(amountColor)

            // Handle dispute status visibility and styling
            if (transaction.isDispute == true && !transaction.disputeStatus.isNullOrEmpty()) {
                binding.tvDisputeStatus.visibility = android.view.View.VISIBLE
                binding.tvDisputeStatus.text = transaction.disputeStatus
                
                val context = binding.root.context
                when (transaction.disputeStatusColor?.lowercase()) {
                    "default" -> {
                        setDisputeBackground(binding.tvDisputeStatus, "#EFEFEF".toColorInt())
                        binding.tvDisputeStatus.setTextColor(context.getColor(com.delhivery.axle.R.color.black_text))
                    }
                    "pending" -> {
                        setDisputeBackground(binding.tvDisputeStatus, context.getColor(com.delhivery.axle.R.color.pending_bg))
                        binding.tvDisputeStatus.setTextColor("#B45309".toColorInt())
                    }
                    "success" -> {
                        setDisputeBackground(binding.tvDisputeStatus, "#DCFCE7".toColorInt())
                        binding.tvDisputeStatus.setTextColor("#16A34A".toColorInt())
                    }
                    "failed" -> {
                        setDisputeBackground(binding.tvDisputeStatus, "#FEE2E2".toColorInt())
                        binding.tvDisputeStatus.setTextColor("#DC2626".toColorInt())
                    }
                    else -> {
                        setDisputeBackground(binding.tvDisputeStatus, context.getColor(com.delhivery.axle.R.color.grey_bg))
                        binding.tvDisputeStatus.setTextColor(context.getColor(com.delhivery.axle.R.color.black_text))
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
                    putExtra(FastagTransactionDetailActivity.EXTRA_VEHICLE_NUMBER, vehicleData?.vehicleNumber ?: "")
                    putExtra(FastagTransactionDetailActivity.EXTRA_TRUCK_TYPE, vehicleData?.truckType ?: "")
                    putExtra(FastagTransactionDetailActivity.EXTRA_TRUCK_SIZE, vehicleData?.truckSize ?: "")
                    putExtra(FastagTransactionDetailActivity.EXTRA_CAPACITY, vehicleData?.capacity ?: 0.0)
                    putExtra(FastagTransactionDetailActivity.EXTRA_OWNERSHIP, vehicleData?.ownership() ?: "")
                }
                context.startActivity(intent)
            }

        }

        private fun setDisputeBackground(view: android.widget.TextView, color: Int) {
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            drawable.cornerRadius = view.context.resources.getDimension(com.delhivery.axle.R.dimen.size_16dp)
            drawable.setColor(color)
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

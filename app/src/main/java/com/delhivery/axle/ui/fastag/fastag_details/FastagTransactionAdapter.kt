package com.delhivery.axle.ui.fastag.fastag_details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.api.response.FastagTransaction
import com.delhivery.axle.databinding.ItemFastagTransactionBinding
import androidx.core.graphics.toColorInt
import com.delhivery.axle.R
import com.delhivery.axle.ui.fastag.qdr.FastagRaiseDisputeActivity
import com.delhivery.axle.utils.DateUtils

class FastagTransactionAdapter(
    private val vehicleNumber: String = ""
) : ListAdapter<FastagTransaction, FastagTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemFastagTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, vehicleNumber)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemFastagTransactionBinding,
        private val vehicleNumber: String
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: FastagTransaction) {
            binding.tvTransactionId.text = transaction.tollName ?: ""
            
            binding.tvTimestamp.text = formatTimestamp(transaction.timestamp ?: "")
            
            binding.tvTollName.text = transaction.txnEventContext ?: transaction.transactionType ?: ""

            val isCredit = transaction.transactionType?.contains("credit", ignoreCase = true) == true ||
                    transaction.transactionType?.contains("recharge", ignoreCase = true) == true ||
                    transaction.transactionType?.contains("refund", ignoreCase = true) == true ||
                    transaction.txnEventContext?.contains("refund", ignoreCase = true) == true
            
            val amountText = if (isCredit) "+₹${transaction.amount?.toInt() ?: 0}" else "-₹${transaction.amount?.toInt() ?: 0}"
            binding.tvAmount.text = amountText
            
            val amountColor = if (isCredit) {
                Color.parseColor("#10B981") // Green for credit/refund
            } else {
                Color.parseColor("#0F172A")
            }
            binding.tvAmount.setTextColor(amountColor)

            if (transaction.isDispute == true && !transaction.disputeStatus.isNullOrEmpty()) {
                binding.tvDisputeStatus.visibility = View.VISIBLE
                binding.tvDisputeStatus.text = transaction.disputeStatus
                
                val context = binding.root.context
                when (transaction.disputeStatusColor?.lowercase()) {
                    "default" -> {
                        setDisputeBackground(binding.tvDisputeStatus, "#EFEFEF".toColorInt())
                        binding.tvDisputeStatus.setTextColor(context.getColor(R.color.black_text))
                    }
                    "pending" -> {
                        setDisputeBackground(binding.tvDisputeStatus, context.getColor(R.color.pending_bg))
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
                        setDisputeBackground(binding.tvDisputeStatus, context.getColor(R.color.grey_bg))
                        binding.tvDisputeStatus.setTextColor(context.getColor(R.color.black_text))
                    }
                }
            } else {
                binding.tvDisputeStatus.visibility = View.GONE
            }

            // Click listener to open transaction detail
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, FastagRaiseDisputeActivity::class.java).apply {
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TXN_ID, transaction.txnId)
                    putExtra(FastagRaiseDisputeActivity.EXTRA_AMOUNT, transaction.amount ?: 0.0)
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TOLL_NAME, transaction.tollName)
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TIMESTAMP, formatTimestamp(transaction.timestamp ?: ""))
                    putExtra(FastagRaiseDisputeActivity.EXTRA_VEHICLE_NUMBER, vehicleNumber)
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TRUCK_TYPE, "")
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TRUCK_SIZE, "")
                    putExtra(FastagRaiseDisputeActivity.EXTRA_CAPACITY, 0.0)
                    putExtra(FastagRaiseDisputeActivity.EXTRA_OWNERSHIP, "")
                    putExtra(FastagRaiseDisputeActivity.EXTRA_TRANSACTION_TYPE, transaction.transactionType)
                }
                context.startActivity(intent)
            }

        }

        private fun setDisputeBackground(view: TextView, color: Int) {
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = view.context.resources.getDimension(R.dimen.size_16dp)
            drawable.setColor(color)
            view.background = drawable
        }

        private fun formatTimestamp(timestamp: String): String {
            return DateUtils.formatFastagTransactionDate(timestamp)
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

package com.delhivery.axle.ui.fastag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.api.response.DisputeType
import com.delhivery.axle.databinding.ItemDisputeIssueBinding

class DisputeIssuesAdapter(
    private val onItemClick: (DisputeType) -> Unit
) : ListAdapter<DisputeType, DisputeIssuesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDisputeIssueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemDisputeIssueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(disputeType: DisputeType) {
            binding.tvDisputeName.text = disputeType.displayName
            
            binding.root.setOnClickListener {
                onItemClick(disputeType)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DisputeType>() {
        override fun areItemsTheSame(oldItem: DisputeType, newItem: DisputeType): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: DisputeType, newItem: DisputeType): Boolean {
            return oldItem == newItem
        }
    }
}

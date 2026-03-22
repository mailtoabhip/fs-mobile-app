package com.delhivery.axle.ui.tripdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.data.tripdetail.TripMilestone
import com.delhivery.axle.databinding.ItemTripMilestoneBinding

/**
 * Adapter for displaying trip milestones in a RecyclerView.
 * Supports dynamic milestone lists for GST and non-GST vendors.
 */
class TripMilestoneAdapter : ListAdapter<TripMilestone, TripMilestoneAdapter.MilestoneViewHolder>(MilestoneDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding = ItemTripMilestoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MilestoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        val milestone = getItem(position)
        val isLastItem = position == itemCount - 1
        holder.bind(milestone, isLastItem)
    }

    class MilestoneViewHolder(
        private val binding: ItemTripMilestoneBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(milestone: TripMilestone, isLastItem: Boolean) {
            binding.milestone = milestone
            binding.isLastItem = isLastItem
            binding.executePendingBindings()
        }
    }

    private class MilestoneDiffCallback : DiffUtil.ItemCallback<TripMilestone>() {
        override fun areItemsTheSame(oldItem: TripMilestone, newItem: TripMilestone): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TripMilestone, newItem: TripMilestone): Boolean {
            return oldItem == newItem
        }
    }
}

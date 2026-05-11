package com.delhivery.axle.ui.home.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.databinding.ItemServiceGroupBinding

/**
 * RecyclerView adapter for displaying service group cards on the Home screen.
 * Uses ListAdapter with DiffUtil for efficient list updates.
 *
 * @param onGroupClick Callback invoked when a service group card is clicked
 */
class ServiceGroupAdapter(
    private val onGroupClick: (ServiceGroup) -> Unit
) : ListAdapter<ServiceGroup, ServiceGroupAdapter.ServiceGroupViewHolder>(ServiceGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceGroupViewHolder {
        val binding = ItemServiceGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServiceGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServiceGroupViewHolder(
        private val binding: ItemServiceGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: ServiceGroup) {
            binding.serviceGroupName.text = group.groupName
            binding.serviceGroupCount.text = binding.root.context.getString(
                R.string.label_services_available, group.serviceCount
            )

            // Map icon string from API to drawable resource
            binding.serviceGroupIcon.setImageResource(getIconDrawable(group.icon))

            binding.root.setOnClickListener { onGroupClick(group) }
        }

        /**
         * Maps the icon identifier from the API response to a local drawable resource.
         * Falls back to a default icon if the identifier is unknown.
         */
        private fun getIconDrawable(icon: String?): Int {
            return when (icon) {
                "truck" -> R.drawable.icon_loads_box
                "wallet" -> R.drawable.fs_icon
                else -> R.drawable.icon_loads_box
            }
        }
    }
}

/**
 * DiffUtil callback for efficient RecyclerView updates.
 * Compares service groups by their unique group_id.
 */
class ServiceGroupDiffCallback : DiffUtil.ItemCallback<ServiceGroup>() {
    override fun areItemsTheSame(oldItem: ServiceGroup, newItem: ServiceGroup): Boolean {
        return oldItem.groupId == newItem.groupId
    }

    override fun areContentsTheSame(oldItem: ServiceGroup, newItem: ServiceGroup): Boolean {
        return oldItem == newItem
    }
}

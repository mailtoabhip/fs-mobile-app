package com.delhivery.axle.ui.invoicereview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.api.response.InvoiceParticular
import com.delhivery.axle.databinding.ItemInvoiceParticularBinding

/**
 * RecyclerView adapter for invoice particulars (line items)
 */
class InvoiceParticularAdapter : ListAdapter<InvoiceParticular, InvoiceParticularAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceParticularBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemInvoiceParticularBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InvoiceParticular) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InvoiceParticular>() {
        override fun areItemsTheSame(oldItem: InvoiceParticular, newItem: InvoiceParticular): Boolean {
            return oldItem.description == newItem.description
        }

        override fun areContentsTheSame(oldItem: InvoiceParticular, newItem: InvoiceParticular): Boolean {
            return oldItem == newItem
        }
    }
}

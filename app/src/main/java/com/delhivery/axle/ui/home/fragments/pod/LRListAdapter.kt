package com.delhivery.axle.ui.home.fragments.pod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.databinding.ItemLrNumberBinding

/**
 * Simple adapter for displaying LR numbers in a list
 */
class LRListAdapter(private val lrNumbers: List<String>) :
    RecyclerView.Adapter<LRListAdapter.LRViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LRViewHolder {
        val binding = ItemLrNumberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LRViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LRViewHolder, position: Int) {
        holder.bind(lrNumbers[position])
    }

    override fun getItemCount() = lrNumbers.size

    class LRViewHolder(private val binding: ItemLrNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lrNumber: String) {
            binding.lrNumber = lrNumber
            binding.executePendingBindings()
        }
    }
}


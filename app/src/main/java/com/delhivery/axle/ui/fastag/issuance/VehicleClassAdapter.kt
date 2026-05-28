package com.delhivery.axle.ui.fastag.issuance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ItemVehicleClassBinding
import com.google.android.material.chip.Chip

class VehicleClassAdapter(
    private val items: List<VehicleClassItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<VehicleClassAdapter.VehicleClassViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleClassViewHolder {
        val binding = ItemVehicleClassBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VehicleClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleClassViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItem(): VehicleClassItem? {
        return if (selectedPosition in items.indices) items[selectedPosition] else null
    }

    fun hasSelection(): Boolean = selectedPosition != -1

    // Counter methods (kept for future use)
    // fun getTotalQuantity(): Int = items.sumOf { it.quantity }
    // fun getSelectedItems(): List<VehicleClassItem> = items.filter { it.quantity > 0 }

    inner class VehicleClassViewHolder(
        private val binding: ItemVehicleClassBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VehicleClassItem, position: Int) {
            binding.item = item
            binding.executePendingBindings()

            // Set indicator color
            binding.colorIndicator.setBackgroundColor(item.indicatorColor)

            // Build chips
            binding.chipContainer.removeAllViews()
            item.vehicleTypes.forEach { type ->
                val chip = Chip(binding.root.context).apply {
                    text = type
                    isClickable = false
                    isCheckable = false
                    setChipBackgroundColorResource(R.color.bg_grey)
                    textSize = 12f
                    setEnsureMinTouchTargetSize(false)
                    chipMinHeight = 0f
                }
                binding.chipContainer.addView(chip)
            }

            // Radio button selection
            binding.rbSelect.isChecked = item.selected

            binding.rbSelect.setOnClickListener {
                selectItem(position)
            }

            // Allow tapping the whole card to select
            binding.cardVehicleClass.setOnClickListener {
                selectItem(position)
            }

            // Counter controls (commented out - kept for future use)
            /*
            binding.btnDecrease.setOnClickListener {
                if (item.quantity > 0) {
                    item.quantity--
                    binding.item = item
                    binding.executePendingBindings()
                    onSelectionChanged()
                }
            }

            binding.btnIncrease.setOnClickListener {
                item.quantity++
                binding.item = item
                binding.executePendingBindings()
                onSelectionChanged()
            }
            */
        }

        private fun selectItem(position: Int) {
            if (selectedPosition == position) return

            val previousPosition = selectedPosition
            selectedPosition = position

            // Deselect previous
            if (previousPosition != -1) {
                items[previousPosition].selected = false
                notifyItemChanged(previousPosition)
            }

            // Select current
            items[position].selected = true
            notifyItemChanged(position)

            onSelectionChanged()
        }
    }
}

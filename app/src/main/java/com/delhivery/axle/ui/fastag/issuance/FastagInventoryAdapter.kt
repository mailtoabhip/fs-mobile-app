package com.delhivery.axle.ui.fastag.issuance

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FastagInventoryItem

class FastagInventoryAdapter(
    private val items: List<FastagInventoryItem>,
    private val colorMapper: (String) -> Int
) : RecyclerView.Adapter<FastagInventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fastag_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorDot: View = itemView.findViewById(R.id.colorDot)
        private val tvClassName: TextView = itemView.findViewById(R.id.tvClassName)
        private val tvVehicleTypes: TextView = itemView.findViewById(R.id.tvVehicleTypes)
        private val tvUnits: TextView = itemView.findViewById(R.id.tvUnits)

        fun bind(item: FastagInventoryItem) {
            tvClassName.text = item.displayName
            tvVehicleTypes.text = item.vehicleTypes.joinToString(", ")
            tvUnits.text = "${item.units} Units"

            val drawable = colorDot.background as? GradientDrawable
            drawable?.setColor(colorMapper(item.colorCode))
        }
    }
}

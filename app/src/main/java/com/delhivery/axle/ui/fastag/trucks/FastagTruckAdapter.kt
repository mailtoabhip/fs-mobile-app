package com.delhivery.axle.ui.fastag.trucks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData

/**
 * Adapter for FASTag trucks list on the FastagTrucksActivity.
 * Shows truck vehicle number, make, FASTag balance, low balance warning, and recharge button.
 */
class FastagTruckAdapter(
    private val onRechargeClick: (HomeTrucksRequestItemData) -> Unit,
    private val onRefreshClick: (HomeTrucksRequestItemData) -> Unit,
    private val onItemClick: (HomeTrucksRequestItemData) -> Unit
) : ListAdapter<HomeTrucksRequestItemData, FastagTruckAdapter.FastagTruckViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FastagTruckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fastag_truck, parent, false)
        return FastagTruckViewHolder(view)
    }

    override fun onBindViewHolder(holder: FastagTruckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FastagTruckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        private val tvTruckMake: TextView = itemView.findViewById(R.id.tvTruckMake)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)
        private val ivRefresh: ImageView = itemView.findViewById(R.id.ivRefresh)
        private val lowBalanceWarning: LinearLayout = itemView.findViewById(R.id.lowBalanceWarning)
        private val btnRecharge: TextView = itemView.findViewById(R.id.btnRecharge)

        fun bind(item: HomeTrucksRequestItemData) {
            tvVehicleNumber.text = item.vehicleNumber
            tvTruckMake.text = item.fastagIssuedBy ?: item.truckName()
            tvBalance.text = "₹${item.fastagBalance ?: "0"}"

            // Show low balance warning if balance < 100
            val balance = item.fastagBalance?.toDoubleOrNull() ?: 0.0
            lowBalanceWarning.visibility = if (balance < 100) View.VISIBLE else View.GONE

            btnRecharge.setOnClickListener { onRechargeClick(item) }
            ivRefresh.setOnClickListener { onRefreshClick(item) }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeTrucksRequestItemData>() {
        override fun areItemsTheSame(
            oldItem: HomeTrucksRequestItemData,
            newItem: HomeTrucksRequestItemData
        ): Boolean = oldItem.fastagTagId == newItem.fastagTagId

        override fun areContentsTheSame(
            oldItem: HomeTrucksRequestItemData,
            newItem: HomeTrucksRequestItemData
        ): Boolean = oldItem == newItem
    }
}

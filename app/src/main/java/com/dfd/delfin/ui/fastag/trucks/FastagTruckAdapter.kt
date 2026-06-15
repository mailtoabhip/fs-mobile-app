package com.dfd.delfin.ui.fastag.trucks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.api.response.FastagVehicle

/**
 * Adapter for FASTag trucks list on the FastagTrucksActivity.
 * Uses FastagVehicle directly from the listing API response.
 */
class FastagTruckAdapter(
    private val onRechargeClick: (FastagVehicle) -> Unit,
    private val onDetailsClick: (FastagVehicle) -> Unit,
    private val onRefreshClick: (FastagVehicle) -> Unit,
    private val onItemClick: (FastagVehicle) -> Unit
) : ListAdapter<FastagVehicle, FastagTruckAdapter.FastagTruckViewHolder>(DiffCallback()) {

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
        private val btnDetails: TextView = itemView.findViewById(R.id.btnDetails)

        fun bind(item: FastagVehicle) {
            tvVehicleNumber.text = item.fastagVrn ?: ""
            tvTruckMake.text = item.vehicleMake ?: ""
            tvBalance.text = "₹${item.fastagBalance ?: "0"}"

            // Show low balance warning if balance < 100
            val balance = item.fastagBalance?.toDoubleOrNull() ?: 0.0
            lowBalanceWarning.visibility = if (balance < 100) View.VISIBLE else View.GONE

            btnRecharge.setOnClickListener { onRechargeClick(item) }
            btnDetails.setOnClickListener { onDetailsClick(item) }
            ivRefresh.setOnClickListener { onRefreshClick(item) }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FastagVehicle>() {
        override fun areItemsTheSame(
            oldItem: FastagVehicle,
            newItem: FastagVehicle
        ): Boolean = oldItem.fastagId == newItem.fastagId

        override fun areContentsTheSame(
            oldItem: FastagVehicle,
            newItem: FastagVehicle
        ): Boolean = oldItem == newItem
    }
}

package com.dfd.delfin.ui.fastag.tagAssignment.assign

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.ui.fastag.tagAssignment.pendingActions.AvailableVehicle

class SelectVehicleAdapter(
    private val onVehicleClick: (AvailableVehicle) -> Unit
) : ListAdapter<AvailableVehicle, SelectVehicleAdapter.VehicleViewHolder>(VehicleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        //private val tvChassisNumber: TextView = itemView.findViewById(R.id.tvChassisNumber)

        fun bind(vehicle: AvailableVehicle) {
            tvVehicleNumber.text = vehicle.vehicleNumber
            //tvChassisNumber.text = vehicle.chassisNumber

            itemView.setOnClickListener {
                onVehicleClick(vehicle)
            }
        }
    }

    class VehicleDiffCallback : DiffUtil.ItemCallback<AvailableVehicle>() {
        override fun areItemsTheSame(oldItem: AvailableVehicle, newItem: AvailableVehicle): Boolean {
            return oldItem.vehicleNumber == newItem.vehicleNumber
        }

        override fun areContentsTheSame(oldItem: AvailableVehicle, newItem: AvailableVehicle): Boolean {
            return oldItem == newItem
        }
    }
}

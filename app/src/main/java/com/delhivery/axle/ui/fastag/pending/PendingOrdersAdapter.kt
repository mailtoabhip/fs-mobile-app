package com.delhivery.axle.ui.fastag.pending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R

class PendingOrdersAdapter : ListAdapter<PendingOrder, PendingOrdersAdapter.OrderViewHolder>(
    OrderDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderMeta: TextView = itemView.findViewById(R.id.tvOrderMeta)
        private val ivExpandCollapse: ImageView = itemView.findViewById(R.id.ivExpandCollapse)
        private val headerLayout: View = itemView.findViewById(R.id.headerLayout)
        private val vehiclesContainer: LinearLayout = itemView.findViewById(R.id.vehiclesContainer)

        fun bind(order: PendingOrder) {
            tvOrderId.text = "Order ID: ${order.orderId}"
            tvOrderMeta.text = "${order.date}  |  ${order.pendingCount} Pending Actions"

            updateExpandState(order.isExpanded)

            headerLayout.setOnClickListener {
                order.isExpanded = !order.isExpanded
                updateExpandState(order.isExpanded)
            }
        }

        private fun updateExpandState(isExpanded: Boolean) {
            val order = getItem(adapterPosition)
            if (isExpanded) {
                ivExpandCollapse.setImageResource(R.drawable.ic_chevron_up)
                vehiclesContainer.visibility = View.VISIBLE
                vehiclesContainer.removeAllViews()
                order.vehicles.forEach { vehicle ->
                    val vehicleView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_pending_vehicle, vehiclesContainer, false)
                    bindVehicleView(vehicleView, vehicle)
                    vehiclesContainer.addView(vehicleView)
                }
            } else {
                ivExpandCollapse.setImageResource(R.drawable.ic_chevron_down)
                vehiclesContainer.visibility = View.GONE
            }
        }

        private fun bindVehicleView(view: View, vehicle: PendingVehicle) {
            val tvVehicleClass: TextView = view.findViewById(R.id.tvVehicleClass)
            val tvVehicleRef: TextView = view.findViewById(R.id.tvVehicleRef)
            val tvActionBadge: TextView = view.findViewById(R.id.tvActionBadge)
            val ivTagIcon: ImageView = view.findViewById(R.id.ivTagIcon)

            tvVehicleClass.text = vehicle.vehicleClass

            // Build reference text
            val refParts = mutableListOf<String>()
            vehicle.vehicleNumber?.let { refParts.add(it) }
            vehicle.referenceId?.let { refParts.add(it) }
            tvVehicleRef.text = refParts.joinToString("  |  ")
            tvVehicleRef.visibility = if (refParts.isEmpty()) View.GONE else View.VISIBLE

            // Set action badge
            tvActionBadge.text = vehicle.actionType.displayName

            val context = view.context
            when (vehicle.actionType) {
                PendingActionType.ASSIGNMENT -> {
                    tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.pending_status))
                    tvActionBadge.setBackgroundResource(R.drawable.bg_badge_assignment)
                    ivTagIcon.setColorFilter(ContextCompat.getColor(context, R.color.pending_status))
                }
                PendingActionType.ACTIVATION -> {
                    tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.pending_status))
                    tvActionBadge.setBackgroundResource(R.drawable.bg_badge_activation)
                    ivTagIcon.setColorFilter(ContextCompat.getColor(context, R.color.pending_status))
                }
                PendingActionType.HANDOVER -> {
                    tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.pending_status))
                    tvActionBadge.setBackgroundResource(R.drawable.bg_badge_handover)
                    ivTagIcon.setColorFilter(ContextCompat.getColor(context, R.color.gray_400))
                }
                PendingActionType.KYV -> {
                    tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.pending_status))
                    tvActionBadge.setBackgroundResource(R.drawable.bg_badge_kyv)
                    ivTagIcon.setColorFilter(ContextCompat.getColor(context, R.color.pending_status))
                }
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<PendingOrder>() {
        override fun areItemsTheSame(oldItem: PendingOrder, newItem: PendingOrder): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: PendingOrder, newItem: PendingOrder): Boolean {
            return oldItem == newItem
        }
    }
}

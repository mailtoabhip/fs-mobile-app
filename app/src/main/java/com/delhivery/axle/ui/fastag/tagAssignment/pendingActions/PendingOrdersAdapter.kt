package com.delhivery.axle.ui.fastag.tagAssignment.pendingActions

import android.content.Context
import android.graphics.Color
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

class PendingOrdersAdapter(
    private val onVehicleClick: (PendingVehicle) -> Unit
) : ListAdapter<PendingOrder, PendingOrdersAdapter.OrderViewHolder>(
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
        private val divider: View = itemView.findViewById(R.id.divider)

        fun bind(order: PendingOrder) {
            tvOrderId.text = "Order ID: ${order.orderId}"
            tvOrderMeta.text = order.date

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
                divider.visibility = View.VISIBLE
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
                divider.visibility = View.GONE
                vehiclesContainer.visibility = View.GONE
            }
        }

        private fun bindVehicleView(view: View, vehicle: PendingVehicle) {
            val tvVehicleClass: TextView = view.findViewById(R.id.tvVehicleClass)
            val tvVehicleRef: TextView = view.findViewById(R.id.tvVehicleRef)
            val tvActionBadge: TextView = view.findViewById(R.id.tvActionBadge)
            val ivTagIcon: ImageView = view.findViewById(R.id.ivTagIcon)

            tvVehicleClass.text = vehicle.vehicleClass

            // Build reference text: vrn | barcodeId (show what's available)
            val refParts = mutableListOf<String>()
            vehicle.vehicleNumber?.let { if (it.isNotEmpty()) refParts.add(it) }
            vehicle.barcodeId?.let { if (it.isNotEmpty()) refParts.add("#$it") }
            tvVehicleRef.text = refParts.joinToString("  |  ")
            tvVehicleRef.visibility = if (refParts.isEmpty()) View.GONE else View.VISIBLE

            // Set action badge text
            tvActionBadge.text = vehicle.actionLabel

            // Set tag icon color based on vehicle class colorCode
            val tagColor = mapColor(view.context, vehicle.colorCode)
            ivTagIcon.setColorFilter(tagColor)

            // Click to open assign vehicle screen
            view.setOnClickListener {
                onVehicleClick(vehicle)
            }
        }

        /**
         * Map color code string to actual color resource.
         */
        private fun mapColor(context: Context, colorCode: String): Int {
            return when (colorCode.uppercase()) {
                "ORANGE" -> context.getColor(R.color.vehicle_class_orange)
                "YELLOW" -> context.getColor(R.color.class_yellow)
                "GREEN" -> context.getColor(R.color.class_green)
                "PINK" -> context.getColor(R.color.class_pink)
                "BLUE" -> context.getColor(R.color.class_blue)
                else -> context.getColor(R.color.class_green)
            }
        }

        /**
         * Create a lighter version of the color for the background.
         */
        private fun adjustAlpha(color: Int, factor: Float): Int {
            val alpha = (255 * factor).toInt()
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
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

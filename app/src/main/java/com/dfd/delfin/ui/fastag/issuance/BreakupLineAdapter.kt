package com.dfd.delfin.ui.fastag.issuance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.api.response.BreakupLineItem

class BreakupLineAdapter(
    private val items: List<BreakupLineItem>
) : RecyclerView.Adapter<BreakupLineAdapter.BreakupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_breakup_line, parent, false)
        return BreakupViewHolder(view)
    }

    override fun onBindViewHolder(holder: BreakupViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BreakupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(item: BreakupLineItem) {
            tvLabel.text = item.label
            tvAmount.text = "₹${item.amount}"
        }
    }
}

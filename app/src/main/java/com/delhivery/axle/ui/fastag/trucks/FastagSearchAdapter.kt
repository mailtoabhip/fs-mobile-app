package com.delhivery.axle.ui.fastag.trucks

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R

/**
 * Adapter for FASTag vehicle search results.
 * Highlights the matching portion of vehicle numbers in bold.
 */
class FastagSearchAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FastagSearchAdapter.SearchViewHolder>() {

    private var results: List<String> = emptyList()
    private var query: String = ""

    fun submitResults(vehicles: List<String>, searchQuery: String) {
        results = vehicles
        query = searchQuery
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fastag_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)

        fun bind(vehicleNumber: String) {
            // Bold the matching query portion
            val spannable = SpannableString(vehicleNumber)
            val matchIndex = vehicleNumber.uppercase().indexOf(query.uppercase())
            if (matchIndex >= 0 && query.isNotEmpty()) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    matchIndex,
                    matchIndex + query.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            tvVehicleNumber.text = spannable

            itemView.setOnClickListener { onItemClick(vehicleNumber) }
        }
    }
}

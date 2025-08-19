package com.delhivery.axle.ui.placementdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R

class FilterDurationAdapter(private val context: Context, private var dataList: ArrayList<Pair<String, Boolean>>, private val listener: FilterItemOnClickListener) : RecyclerView.Adapter<FilterDurationAdapter.ViewHolder>() {
    var selectedPosition = -1
    var lastSelectedPosition = -1
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var durationItem : TextView = view.findViewById(R.id.durationItem) as TextView
        var mConstraintLayout: ConstraintLayout = view.findViewById(R.id.clDuration) as ConstraintLayout
        var selectedImage: ImageView = view.findViewById(R.id.selectedImage) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_filter_duration_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.durationItem.text =  dataList[position].first
        if(position==0){
            listener.handleItemClick(dataList[position])
            holder.selectedImage.visibility = View.VISIBLE
            holder.mConstraintLayout.background =ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_red)
        }else{
            holder.selectedImage.visibility = View.GONE
            holder.mConstraintLayout.background =null
        }
        holder.mConstraintLayout.setOnClickListener { v ->
            lastSelectedPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
        if (selectedPosition == holder.bindingAdapterPosition) {
            listener.handleItemClick(dataList[position])
            holder.mConstraintLayout.background =ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_red)
            holder.selectedImage.visibility = View.VISIBLE
        } else {
            holder.mConstraintLayout.background =null
            holder.selectedImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}

interface FilterItemOnClickListener{
    fun handleItemClick(item : Pair<String,Boolean?>)
}


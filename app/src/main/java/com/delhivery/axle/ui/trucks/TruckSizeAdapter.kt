package com.delhivery.axle.ui.trucks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.databinding.TruckSizeItemBinding

class TruckSizeAdapter : BaseAdapter() {
    private val items: MutableList<TruckResponseArray> = mutableListOf()
    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ItemRowHolder
        val view: View

        if (convertView == null) {
            val binding = TruckSizeItemBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
            holder = ItemRowHolder(binding)
            view = binding.root
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ItemRowHolder
        }

        val item = getItem(position)
        holder.binding.textSize.text = item.truckUuid
        holder.binding.textCapacity.text = " (${item.defaultMG} MT)"
        return view
    }

    /**
     * Clear data set and set items
     */
    fun setItems(items: List<TruckResponseArray>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private class ItemRowHolder(val binding: TruckSizeItemBinding)
}
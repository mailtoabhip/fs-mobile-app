package com.delhivery.axle.ui.businessverification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.delhivery.axle.R

/**
 *  LV adapter for [Upload dialog]
 */
class DocUploadAdapter : BaseAdapter() {

    private val items: MutableList<Pair<String,String>> = mutableListOf()
    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.view_business_doc_item, parent, false)
            vh = ItemRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val item = getItem(position)
        vh.label.text = item.first
        vh.size.text = item.second+" KB"

        vh.remove.setOnClickListener {
            items.removeAt(position)
            notifyDataSetChanged()
        }
        return view
    }

    /**
     * Clear data set and set items
     */
    fun setItems(items: List<Pair<String,String>>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private class ItemRowHolder(row: View?) {
        val label: TextView = row?.findViewById(R.id.doc_name) as TextView
        val size: TextView = row?.findViewById(R.id.doc_size) as TextView
        val remove: ImageView = row?.findViewById(R.id.icon_remove) as ImageView
    }
}

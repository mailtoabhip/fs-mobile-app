package com.dfd.delfin.ui.ledger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.dfd.delfin.R
import com.dfd.delfin.data.ledger.LedgerSpinnerOptions

class LedgerSpinnerAdapter: BaseAdapter() {
    private val items: MutableList<LedgerSpinnerOptions> = mutableListOf()
    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.spinner_item, parent, false)
            vh = ItemRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val item = getItem(position)
        vh.label.text = item.option
        return view
    }

    /**
     * Clear data set and set items
     */
    fun setItems(items: List<LedgerSpinnerOptions>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView = row?.findViewById(R.id.text) as TextView

    }
}
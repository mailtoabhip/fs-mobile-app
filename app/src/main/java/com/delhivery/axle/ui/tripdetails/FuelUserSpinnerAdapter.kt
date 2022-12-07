package com.delhivery.axle.ui.tripdetails

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.FuelUserSpinnerOptions
import com.delhivery.axle.data.ledger.LedgerSpinnerOptions

class FuelUserSpinnerAdapter : BaseAdapter() {
    private val items: MutableList<FuelUserSpinnerOptions> = mutableListOf()
    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.spinner_item_for_fuel_card_user, parent, false)
            vh = ItemRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val item = getItem(position)
        vh.userName.text = item.userName
        vh.userType.text = item.userType

        if (item.userName == "Different Number"){
            vh.userImage.visibility = View.VISIBLE
            val spannable = SpannableStringBuilder("Different Number")
            spannable.setSpan(
                    UnderlineSpan(),
                    0, // start
                    16, // end
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            vh.userName.setTextColor(Color.BLUE)
            vh.userName.text = spannable

        }

        return view
    }


    /**
     * Clear data set and set items
     */
    fun setItems(items: List<FuelUserSpinnerOptions>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    private class ItemRowHolder(row: View?) {
        val userImage :AppCompatImageView = row?.findViewById(R.id.userImage) as AppCompatImageView
        val userName: TextView = row?.findViewById(R.id.user_name) as TextView
        val userType: TextView = row?.findViewById(R.id.user_type) as TextView

    }
}
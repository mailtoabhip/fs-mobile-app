
package com.delhivery.axle.ui.biddetails

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R


class AddressDetailAdapter(private val dataList: ArrayList<Pair<String, String?>>) : RecyclerView.Adapter<AddressDetailAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var add1: TextView = view.findViewById(R.id.head) as TextView
        var mLinearLayout: LinearLayout= view.findViewById(R.id.mLay) as LinearLayout
        var add2: TextView = view.findViewById(R.id.subHead) as TextView
        var dotLine: View = view.findViewById(R.id.dotLine) as View
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_address_detail_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.add1.text =  dataList[position].first
        holder.add2.text =  dataList[position].second

        holder.mLinearLayout.measure(
                View.MeasureSpec.makeMeasureSpec(Resources.getSystem().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        val layoutHeight = holder.mLinearLayout.measuredHeight

        holder.dotLine.getLayoutParams().height = layoutHeight
        holder.dotLine.requestLayout()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

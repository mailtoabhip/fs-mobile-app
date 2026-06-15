
package com.dfd.delfin.ui.biddetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R


class StopAdapter(private val dataList: List<Triple<Pair<Int,String?>,String?, String?>>) : RecyclerView.Adapter<StopAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
          var city: TextView = view.findViewById(R.id.city) as TextView
        var img: View = view.findViewById(R.id.img) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_stop_detail_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position==0){
            holder.img.visibility= View.GONE
        }else{
            holder.img.visibility= View.VISIBLE
        }
        holder.city.text =  dataList[position].first.second
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

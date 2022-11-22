package com.delhivery.axle.ui.contractDetails

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HaltCenters
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData

class ContractsRouteDetailsAdapter(private val dataList: List<HaltCenters>,private val transaction:HomeBidsRequestItemData?,private val context: Context) : RecyclerView.Adapter<ContractsRouteDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      var hubCity: TextView = view.findViewById(R.id.tvHubCity) as TextView
      var hubState: TextView = view.findViewById(R.id.tvState) as TextView
      var arvTime: TextView = view.findViewById(R.id.arrivalTime) as TextView
      var depTime: TextView = view.findViewById(R.id.departureTime) as TextView
      var lineSep : ImageView = view .findViewById(R.id.lineSep) as ImageView
      var clTravelTime: View = view.findViewById(R.id.clTravelTime)
      var timeTravel: TextView  = view.findViewById(R.id.timeInterval) as TextView
      var hubIcon : ImageView = view .findViewById(R.id.hubIcon) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_contracts_hub_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      if(position==0){
        holder.clTravelTime.visibility= View.GONE
        holder.timeTravel.visibility= View.GONE
        holder.lineSep.visibility= View.GONE
      }else{
        holder.clTravelTime.visibility= View.VISIBLE
        holder.timeTravel.visibility= View.VISIBLE
        holder.lineSep.visibility= View.VISIBLE
      }
      holder.hubCity.text = dataList[position].name
      holder.hubState.text = dataList[position].state
      holder.arvTime.text = dataList[position].relEta
      holder.depTime.text = dataList[position].relEtd
      holder.timeTravel.text = "Travel Time - "+dataList[position].pastTravelHrs+ " hrs"

      if(transaction?.transactionStatus=="cancelled"){
        holder.timeTravel.setTextColor(ContextCompat.getColor(context,R.color.heading_black))
        holder.clTravelTime.background = ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_light_grey)
        holder.hubIcon.setImageDrawable(
          ContextCompat.getDrawable(
            context,
            R.drawable.ic_black_hub
          )
        )
      }else{
        holder.timeTravel.setTextColor(ContextCompat.getColor(context,R.color.dark_blue))
        holder.clTravelTime.background = ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_light_blue)
        holder.hubIcon.setImageDrawable(
          ContextCompat.getDrawable(
            context,
            R.drawable.ic_hub_route
          )
        )
      }
    }
    override fun getItemCount(): Int {
      return dataList.size
    }
  }

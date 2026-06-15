package com.dfd.delfin.ui.contractDetails

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.dfd.delfin.data.home.bids.HaltCenters
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty

class PlacementsContractsRouteDetailsAdapter(
    private val dataList: List<HaltCenters>,
    private val transaction: HomeBidsRequestItemData?,
    private val context: Context
) : RecyclerView.Adapter<PlacementsContractsRouteDetailsAdapter.ViewHolder>() {

    private val HR_CONST = " hr"
    private val HRS_CONST = " hrs"

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var hubCity: TextView = view.findViewById(R.id.tvHubCity) as TextView
        var hubState: TextView = view.findViewById(R.id.tvState) as TextView
        var arvTime: TextView = view.findViewById(R.id.arrivalTime) as TextView
        var depTime: TextView = view.findViewById(R.id.departureTime) as TextView
        var lineSep: ImageView = view.findViewById(R.id.lineSep) as ImageView
        var clTravelTime: View = view.findViewById(R.id.clTravelTime)
        var timeTravel: TextView = view.findViewById(R.id.timeInterval) as TextView
        var hubIcon: ImageView = view.findViewById(R.id.hubIcon) as ImageView
        var tvMapView: TextView = view.findViewById(R.id.tvMapView) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_placements_contracts_hub_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.clTravelTime.visibility = View.GONE
            holder.timeTravel.visibility = View.GONE
            holder.lineSep.visibility = View.GONE
        } else {
            holder.clTravelTime.visibility = View.VISIBLE
            holder.timeTravel.visibility = View.VISIBLE
            holder.lineSep.visibility = View.VISIBLE
        }
        holder.hubCity.text = dataList[position].name?.split("(")?.get(0) ?: dataList[position].name
        //
        if (dataList[position].state.isNotNullOrEmpty()) {
            holder.hubState.visibility = View.VISIBLE
            holder.hubState.text = dataList[position].state
        } else {
            holder.hubState.visibility = View.GONE
        }

        //val etaList = dataList[position].relEta?.split(",")
        //holder.arvTime.text ="${etaList?.getOrNull(0) ?: ""}\n${etaList?.getOrNull(1) ?: ""}"

        holder.arvTime.text =
            transaction?.getTimeOfContractsForPlacements(dataList[position].relEta ?: "")


        //val etdList = dataList[position].relEtd?.split(",")
        //holder.depTime.text = "${etdList?.getOrNull(0) ?: ""}\n${etdList?.getOrNull(1) ?: ""}"


        holder.depTime.text =
            transaction?.getTimeOfContractsForPlacements(dataList[position].relEtd ?: "")

        //logic for "hr" and "hrs"
        try {
            val pastTravelHrsDouble = dataList[position].pastTravelHrs?.toDoubleOrNull()
            //
            if (pastTravelHrsDouble == null || pastTravelHrsDouble == 0.0) {
                holder.clTravelTime.visibility = View.GONE
                holder.timeTravel.visibility = View.GONE
            } else {
                holder.clTravelTime.visibility = View.VISIBLE
                holder.timeTravel.visibility = View.VISIBLE

                //check for "hrs" vs "hr"
                if (pastTravelHrsDouble < 1.0) {
                    holder.timeTravel.text =
                        "Travel Time - " + dataList[position].pastTravelHrs + HR_CONST
                } else if (pastTravelHrsDouble == 1.0) {
                    holder.timeTravel.text =
                        "Travel Time - " + dataList[position].pastTravelHrs + HR_CONST
                } else {
                    holder.timeTravel.text =
                        "Travel Time - " + dataList[position].pastTravelHrs + HRS_CONST
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //
            holder.clTravelTime.visibility = View.GONE
            holder.timeTravel.visibility = View.GONE
        }

        val lat = dataList[position].latitude
        val long = dataList[position].longitude

        if (lat.isNotNullOrEmpty() && long.isNotNullOrEmpty()) {
            holder.tvMapView.visibility = View.VISIBLE
            holder.tvMapView.setOnClickListener {
                try {
                    val gmmIntentUri = Uri.parse(
                        "geo:0,0?q=$lat,$long" + "(" + (dataList[position].name?.split("(")?.get(0)
                            ?: dataList[position].name)?.replace("_", " ") + ")"
                    )
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.tvMapView.visibility = View.GONE
        }




        if (transaction?.transactionStatus == "cancelled") {
            holder.timeTravel.setTextColor(ContextCompat.getColor(context, R.color.heading_black))
            holder.clTravelTime.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_grey)
            holder.hubIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_black_hub
                )
            )
            holder.tvMapView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.background_dark_grey
                )
            )
        } else {
            holder.timeTravel.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
            holder.clTravelTime.background =
                ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_blue)
            holder.hubIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_hub_route
                )
            )
            holder.tvMapView.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
        }


        //This logic is no more required as destination pas travel time will be used
        //check if array size only 2 as only pickup and drop are present - hide the travel time field
//      if(dataList.size == 2) {
//        holder.clTravelTime.visibility= View.GONE
//        holder.timeTravel.visibility = View.GONE
//      }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

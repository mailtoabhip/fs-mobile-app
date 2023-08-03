package com.delhivery.axle.ui.contractDetails

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
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.TransactionStatus
import com.delhivery.axle.data.home.bids.HaltCenters
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.SecondaryReportingCenters

class ContractsIntracityAdHocRCAdapter (private val dataList: List<SecondaryReportingCenters>,private val transaction: HomeBidsRequestItemData?,private val context: Context) : RecyclerView.Adapter<ContractsIntracityAdHocRCAdapter.ViewHolder>() {

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var intraCityAdHocTvHubCity: TextView = view.findViewById(R.id.intraCityAdHocTvHubCity) as TextView
    var intraCityAdHocTvCity: TextView = view.findViewById(R.id.intraCityAdHocTvCity) as TextView
    var intraCityAdHocTvState: TextView = view.findViewById(R.id.intraCityAdHocTvState) as TextView
    var intraCityAdHocTvMapView: TextView = view.findViewById(R.id.intraCityAdHocTvMapView) as TextView
    var intraCityAdHocHubIcon : ImageView = view .findViewById(R.id.intraCityAdHocHubIcon) as ImageView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_intracity_adhoc_reporting_centers, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    holder.intraCityAdHocTvHubCity.text = dataList[position].originCenterName
    holder.intraCityAdHocTvCity.text = dataList[position].originCity
    holder.intraCityAdHocTvState.text = dataList[position].originState
    val lat = dataList[position].latitude
    val long = dataList[position].longitude
    holder.intraCityAdHocTvMapView.setOnClickListener {
      try {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$lat,$long"+"(" + (dataList[position].originCenterName?.split("(")?.get(0) ?: dataList[position].originCenterName )?.replace("_"," ")+ ")")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
      } catch (e: Exception) {
        Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show()
      }
    }

    if(transaction?.transactionStatus==TransactionStatus.Cancelled.statusId){
      holder.intraCityAdHocHubIcon.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_black_hub
        )
      )
      holder.intraCityAdHocTvMapView.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
    }else{
      holder.intraCityAdHocHubIcon.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_hub_route
        )
      )
      holder.intraCityAdHocTvMapView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    }
  }
  override fun getItemCount(): Int {
    return dataList.size
  }
}
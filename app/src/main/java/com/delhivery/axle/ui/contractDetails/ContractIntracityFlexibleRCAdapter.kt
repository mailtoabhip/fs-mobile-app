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
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.SecondaryReportingCenters

class ContractIntracityFlexibleRCAdapter (private val dataList: List<SecondaryReportingCenters>,private val transaction: HomeBidsRequestItemData?,private val context: Context) : RecyclerView.Adapter<ContractIntracityFlexibleRCAdapter.ViewHolder>() {

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var intraCityFlexibleTvHubCity: TextView = view.findViewById(R.id.intraCityFlexibleTvHubCity) as TextView
    var intraCityFlexibleTvCity: TextView = view.findViewById(R.id.intraCityFlexibleTvCity) as TextView
    var intraCityFlexibleTvState: TextView = view.findViewById(R.id.intraCityFlexibleTvState) as TextView
    var intraCityFlexibleTvMapView: TextView = view.findViewById(R.id.intraCityFlexibleTvMapView) as TextView
    var intraCityFlexibleHubIcon : ImageView = view .findViewById(R.id.intraCityFlexibleHubIcon) as ImageView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_intracity_flexible_reporting_centers, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    holder.intraCityFlexibleTvHubCity.text = dataList[position].originCenterName
    holder.intraCityFlexibleTvCity.text = dataList[position].originCity
    holder.intraCityFlexibleTvState.text = dataList[position].originState
    val lat = dataList[position].latitude
    val long = dataList[position].longitude
    holder.intraCityFlexibleTvMapView.setOnClickListener {
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
      holder.intraCityFlexibleHubIcon.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_black_hub
        )
      )
      holder.intraCityFlexibleTvMapView.setTextColor(ContextCompat.getColor(context, R.color.background_dark_grey))
    }else{
      holder.intraCityFlexibleHubIcon.setImageDrawable(
        ContextCompat.getDrawable(
          context,
          R.drawable.ic_hub_route
        )
      )
      holder.intraCityFlexibleTvMapView.setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
    }
  }
  override fun getItemCount(): Int {
    return dataList.size
  }
}
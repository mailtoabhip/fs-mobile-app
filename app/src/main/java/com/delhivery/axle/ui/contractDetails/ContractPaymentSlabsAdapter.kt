package com.delhivery.axle.ui.contractDetails

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.PaymentSlabs

class ContractPaymentSlabsAdapter(private val dataList: List<PaymentSlabs>,private val transaction: HomeBidsRequestItemData?,private val context: Context) : RecyclerView.Adapter<ContractPaymentSlabsAdapter.ViewHolder>() {

  private var mExpanded:Boolean=false

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var clPaymentSlab: View = view.findViewById(R.id.clPaymentSlab)
    var tvDistance: TextView = view.findViewById(R.id.tvDistance) as TextView
    var tvMonthlyPayout: TextView = view.findViewById(R.id.tvMonthlyPayout) as TextView
    var separator: View = view.findViewById(R.id.separator)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_contracts_payment_slabs, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    if(position==0){
      holder.tvDistance.text= "Kms"
      holder.tvMonthlyPayout.text= "Monthly Payout"
      holder.tvDistance.setTypeface(null, Typeface.BOLD)
      holder.tvMonthlyPayout.setTypeface(null, Typeface.BOLD)
      holder.clPaymentSlab.background = ContextCompat.getDrawable(context,R.color.lighter_blue)
      holder.separator.visibility = View.VISIBLE
    }else{
      holder.tvDistance.setTypeface(null, Typeface.NORMAL)
      holder.tvMonthlyPayout.setTypeface(null, Typeface.NORMAL)
      holder.tvDistance.text = dataList[position].distance
      holder.tvMonthlyPayout.text = dataList[position].monthlyPayout
      if(transaction?.intracityKms==dataList[position].distance){
        holder.clPaymentSlab.background = ContextCompat.getDrawable(context,R.color.dark_light_blue)
      }else{
        holder.clPaymentSlab.background = ContextCompat.getDrawable(context,R.color.lighter_blue)
      }
      holder.separator.visibility = View.GONE
    }
  }

  fun expand(){
      mExpanded = true
      notifyDataSetChanged()
  }
  override fun getItemCount(): Int {
    return if (mExpanded) dataList.size else if(dataList.size<4) dataList.size else 4
  }
}
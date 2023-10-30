package com.delhivery.axle.ui.biddetails

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.biddetail.OPEN_CONFIRMED_BID
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemAction
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.bids.BidsActivity
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.tripdetails.*
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

abstract class BaseBulkBidsRVAdapterVH<out B: ViewDataBinding,
    IT : BaseBulkBidSummaryRVAdapterItem<*>>(binding: B) : BaseViewHolder<B>(binding) {
abstract fun bind(
        item: IT,
        _interface: BulkBidsRVAdapterInterface
)
    /**
     * Add Click Listener for Action
     */
    protected fun View.clickToAction(
            actionId: String,
            item: IT,
            position: Int,
            _interface: BulkBidsRVAdapterInterface
    ) = setOnClickListener{ action(actionId, item, position, _interface)}

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        position: Int,
        _interface: BulkBidsRVAdapterInterface
    ) = post {_interface.handleAction(actionId, position, item) }
}

/**
 * Bulk Bids summary item view holder
 */
class BulkBidsSummaryItemVH(binding: ViewBidDetailItemBinding) :
        BaseBulkBidsRVAdapterVH<ViewBidDetailItemBinding, BulkBidSummaryItem>(binding) {
    override fun bind(
        item: BulkBidSummaryItem,
        _interface: BulkBidsRVAdapterInterface) {
        binding.item = item.data
        binding.textVehicleType.text = item.data.vehicleType
        if(!item.data.vehicleNumber.isNullOrEmpty()) {
            binding.VehicleNumLayout.removeAllViews()

            for (i in item.data.vehicleNumber) {
              val textView = TextView(context)
              if(i.key!="null") {
                textView.text = i.key ?: ""
                textView.setPadding(0,4,0,2)
                textView.setTextColor(ContextCompat.getColor(context,R.color.blue))
                textView.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_open_external_link)
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                textView.setOnClickListener()
                {
                  if(!item.data.childTransactionId.isNullOrEmpty()) {
                    context.startActivity(tripDetailsIntent(i.value, context))
                  }
                }
                // Add TextView to LinearLayout
                binding.VehicleNumLayout.addView(textView)
              }else{
                textView.text = ""
              }
        }
    }
        binding.expandableLayout.clickToAction(EXPAND_CARD,item, bindingAdapterPosition, _interface)
        binding.expandButton.clickToAction(EXPAND_CARD,item, bindingAdapterPosition, _interface)
    }
}

/**
 * Bulk Bid summary progress item view holder
 * */
class BulkBidSummaryProgressItemVH(binding: ViewProgressItemBinding) :
        BaseBulkBidsRVAdapterVH<ViewProgressItemBinding,BulkBidSummaryProgressItem>(binding) {
    override fun bind(item: BulkBidSummaryProgressItem, _interface: BulkBidsRVAdapterInterface) {

    }
}

/**
 * Bulk Bid summary timeout item view holder
 * */
internal class BulkBidSummaryTimeOutItemVH(binding: ViewTimeOutItemBinding) :
        BaseBulkBidsRVAdapterVH<ViewTimeOutItemBinding, BulkBidTimeoutItem>(binding) {
    override fun bind(item: BulkBidTimeoutItem, _interface: BulkBidsRVAdapterInterface) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
    }

}
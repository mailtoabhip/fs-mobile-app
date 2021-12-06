package com.delhivery.axle.ui.bids

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.DmtBidSummaryItemData
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.databinding.ViewBidCreateEditItemBinding
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.biddetails.DmtBidsAdapterInterface
import kotlin.math.abs

abstract class BaseDmtBidsRVAdapterVH<out B: ViewDataBinding,
        IT : BaseDmtBidSummaryRVAdapterItem<*>>(binding: B) : BaseViewHolder<B>(binding) {

    abstract fun bind(
        item: IT,
        _interface: DmtBidsAdapterInterface
    )
    /**
     * Add Click Listener for Action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        position: Int,
        _interface: DmtBidsAdapterInterface
    ) = setOnClickListener{ action(actionId, item, position, _interface)}

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        position: Int,
        _interface: DmtBidsAdapterInterface
    ) = post {_interface.handleAction(actionId, position, item) }

}

class DmtBidsSummaryItemVH(binding: ViewBidCreateEditItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewBidCreateEditItemBinding, DmtBidSummaryItem>(binding) {
    override fun bind(
        item: DmtBidSummaryItem,
        _interface: DmtBidsAdapterInterface
    ) {
        binding.item = item.data
      //  val bidData = item.data as DmtBidSummaryItemData

      //  binding.spinnerVehicleType.selectedView = item.data.vehicleType
        binding.editTrucks.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.length>0) {
                  binding.item!!.truckCount = Integer.parseInt(s.toString())
                }
            }
        })
        binding.editPmtAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.length>0) {
                    binding.item!!.pmtRate = (s.toString().toDouble())
                }
            }
        })
        binding.expandButton.clickToAction(EXPAND_CARD,item, adapterPosition, _interface)
        binding.deleteItem.clickToAction(DELETE_ITEM,item,adapterPosition,_interface)

    }
}

class DmtBidSummaryProgressItemVH(binding: ViewProgressItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewProgressItemBinding,DmtBidSummaryProgressItem>(binding) {
    override fun bind(item: DmtBidSummaryProgressItem, _interface: DmtBidsAdapterInterface) {

    }
}

/**
 * Dmt Bid summary timeout item view holder
 * */
internal class DmtBidSummaryTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewTimeOutItemBinding, BulkBidTimeoutItem>(binding) {
    override fun bind(item: BulkBidTimeoutItem, _interface: DmtBidsAdapterInterface) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
    }

}
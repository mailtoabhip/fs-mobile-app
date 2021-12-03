package com.delhivery.axle.ui.bids

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.databinding.ViewDmtBidItemBinding
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

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

class DmtBidsSummaryItemVH(binding: ViewDmtBidItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewDmtBidItemBinding, DmtBidSummaryItem>(binding) {
    override fun bind(
        item: DmtBidSummaryItem,
        _interface: DmtBidsAdapterInterface) {
        binding.item = item.data


       // binding.spinnerVehicleType.selectedView = item.data.vehicleType

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
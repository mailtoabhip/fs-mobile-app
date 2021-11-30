package com.delhivery.axle.ui.biddetails

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemAction
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.tripdetails.*


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
        binding.expandButton.clickToAction(EXPAND_CARD,item, adapterPosition, _interface)

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
        binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
    }

}
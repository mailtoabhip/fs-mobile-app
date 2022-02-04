package com.delhivery.axle.ui.kyc.address

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder



abstract class BaseAddressRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseAddressRVAdapterItem<*>>(
    binding: B
) :
    BaseViewHolder<B>(binding) {
    abstract fun bind(
        item: IT,
        _interface: AddressRVAdapterInterface
    )

    /**
     * Add on click listener for action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        _interface: AddressRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, _interface) }

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        _interface: AddressRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item) }
}


/**
 * Gst data item view holder
 */
class AddressDataItemVH(binding: ViewAddressRequestItemBinding) :
    BaseAddressRVAdapterViewHolder<ViewAddressRequestItemBinding, AddressDataItem>(binding) {
    override fun bind(
        item: AddressDataItem,
        _interface: AddressRVAdapterInterface
    ) {
        binding.title= item.data.address
        binding.root.clickToAction(HomeTripsRequestAction_ViewDetails, item, _interface)
    }
}

/**
 * Search warning item view holder
 */
internal class AddressWarningItemVH(binding: ViewWarningItemBinding) :
    BaseAddressRVAdapterViewHolder<ViewWarningItemBinding, AddressWarningItem>(binding) {
    override fun bind(
        item: AddressWarningItem,
        _interface: AddressRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    }
}

/**
 * Search timeout view holder
 */
internal class AddressTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseAddressRVAdapterViewHolder<ViewTimeOutItemBinding, AddressTimeoutItem>(binding) {
    override fun bind(
        item: AddressTimeoutItem,
        _interface: AddressRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    }
}

/**
 * Progress inline viewholder
// */
//internal class AddressProgressItemVH(binding: ViewAddressProgressItemBinding) :
//    BaseAddressRVAdapterViewHolder<ViewGstProgressItemBinding, GstProgressItem>(
//        binding
//    ) {
//    override fun bind(
//        item: GstProgressItem,
//        _interface: GstRVAdapterInterface
//    ) {
//
//    }
//}
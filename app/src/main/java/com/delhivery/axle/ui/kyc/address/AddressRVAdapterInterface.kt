package com.delhivery.axle.ui.kyc.address

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

interface AddressRVAdapterInterface : BaseDataRVAdapter.ItemClickListener<BaseAddressRVAdapterItem<*>> {

    override fun onItemClicked(
        item: BaseAddressRVAdapterItem<*>
    ) {
    }

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        item: BaseAddressRVAdapterItem<*>
    )
}
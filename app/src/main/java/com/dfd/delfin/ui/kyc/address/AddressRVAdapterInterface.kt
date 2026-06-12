package com.dfd.delfin.ui.kyc.address

import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

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

    fun editItem(item: AddressDataItem)

    fun selectItem(item: AddressDataItem, position: Int)

}
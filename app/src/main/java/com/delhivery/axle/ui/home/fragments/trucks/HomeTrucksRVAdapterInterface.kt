package com.delhivery.axle.ui.home.fragments.trucks

import com.delhivery.axle.data.home.trucks.HomeTrucksRequestAction_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

interface HomeTrucksRVAdapterInterface : BaseDataRVAdapter.ItemClickListener<BaseHomeTrucksRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseHomeTrucksRVAdapterItem<*>) {
        if (item.type == HomeTrucksRVAdapterItemType.Request) {
            handleAction(HomeTrucksRequestAction_ViewDetails, item)
        }
    }

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        item: BaseHomeTrucksRVAdapterItem<*>
    )

    /**
     * Handle specific action with item position
     */
    fun handleAction(
        actionId: String,
        item: BaseHomeTrucksRVAdapterItem<*>,
        position: Int
    )
}
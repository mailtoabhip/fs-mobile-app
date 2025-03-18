package com.delhivery.axle.ui.home.fragments.placements

import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.placements.HomePlacementRequested_ViewDetails
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.home.fragments.loads.BaseHomeLoadsRVAdapterItem
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType

interface HomePlacementsRVAdapterInterface :
    BaseDataRVAdapter.ItemClickListener<BaseHomePlacementsRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseHomePlacementsRVAdapterItem<*>) {
        if (item.type == HomePlacementsRVAdapterItemType.IntracityAdhoc || item.type == HomePlacementsRVAdapterItemType.IntracityContracts || item.type == HomePlacementsRVAdapterItemType.IntercityAdhoc||item.type == HomePlacementsRVAdapterItemType.IntercityContracts) {
            handleAction(HomePlacementRequested_ViewDetails, item)
        }
    }

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        item: BaseHomePlacementsRVAdapterItem<*>
    )

    /**
     * Handle specific action with item position
     */
    fun handleAction(
        actionId: String,
        item: BaseHomePlacementsRVAdapterItem<*>,
        position: Int
    )

}
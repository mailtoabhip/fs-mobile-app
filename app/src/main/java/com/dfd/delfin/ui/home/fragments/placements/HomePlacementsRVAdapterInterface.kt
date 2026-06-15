package com.dfd.delfin.ui.home.fragments.placements

import com.dfd.delfin.data.home.placements.HomePlacementRequested_ViewDetails
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

interface HomePlacementsRVAdapterInterface :
    BaseDataRVAdapter.ItemClickListener<BaseHomePlacementsRVAdapterItem<*>> {
    override fun onItemClicked(item: BaseHomePlacementsRVAdapterItem<*>) {
        if (item.type == HomePlacementsRVAdapterItemType.PlacementItem ) {
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
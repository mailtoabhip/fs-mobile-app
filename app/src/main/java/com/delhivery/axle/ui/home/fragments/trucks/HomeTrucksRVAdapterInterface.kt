package com.delhivery.axle.ui.home.fragments.trucks

import com.delhivery.axle.data.home.trucks.HomeTrucksRequestAction_ViewDetails
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.loadAlert.HomeLoadAlertRequestItemData

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

    fun callShareRate(data: HomeTrucksRequestItemData?, itemTD:String?, offerTD:String?,offerid:String?)

    fun getTotalOffers(data: HomeTrucksRequestItemData?)

    fun getBannerStatus():Boolean?

    fun callRewards()

    fun gettotal():Int

     fun settotal(total:Int)
}
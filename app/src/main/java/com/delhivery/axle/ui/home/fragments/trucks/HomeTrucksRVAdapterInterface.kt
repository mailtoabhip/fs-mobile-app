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

    fun getTotalOffers(origin_id:String?, dest_id:String?, tid:String?):Triple<Pair<Boolean?,String?>, String?, String?>?

    fun getBannerStatus():Boolean?

    fun callRewards()

}
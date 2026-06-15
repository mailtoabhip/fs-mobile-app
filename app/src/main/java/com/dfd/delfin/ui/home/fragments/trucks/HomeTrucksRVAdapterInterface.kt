package com.dfd.delfin.ui.home.fragments.trucks

import com.dfd.delfin.data.home.trucks.HomeTrucksRequestAction_ViewDetails
import com.dfd.delfin.data.home.trucks.HomeTrucksRequestItemData
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

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

    fun callShareRate(data: HomeTrucksRequestItemData?, itemTD:String?, offerTD:String?,offerid:String?,amt:String?)

    fun getTotalOffers(data: HomeTrucksRequestItemData?)

    fun getBannerStatus():Boolean?

    fun callRewards()

    fun gettotal():Int

     fun settotal(total:Int)
     
     /**
      * Refresh FASTag balance
      */
     fun refreshFastagBalance(tagId: String)
     
     /**
      * Open FASTag transaction details
      */
     fun openFastagDetails(data: HomeTrucksRequestItemData)
     
     /**
      * Open FASTag recharge screen
      */
     fun openFastagRecharge(data: HomeTrucksRequestItemData)
}
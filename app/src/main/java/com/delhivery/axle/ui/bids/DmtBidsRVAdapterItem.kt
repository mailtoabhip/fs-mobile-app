package com.delhivery.axle.ui.bids

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.bids.DmtBidSummaryItemData
import com.delhivery.axle.data.bids.DmtBidSummaryProgressItemData
import com.delhivery.axle.data.home.bids.HomeBidsTimeOutItemData

enum class DmtBidsSummaryRVAdapterItemType(val typeId:Int){
    Summary(0),
    Progress(1),
    Timeout(2);

    companion object{
        fun byTypeId(typeId: Int) = values().filter { typeId === it.typeId }.firstOrNull()
    }
}



abstract class BaseDmtBidSummaryRVAdapterItem<D: BaseKeyTypeModel<String>>(
    val type: DmtBidsSummaryRVAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>(){
    override fun key() = data.key()
}

/**
 * Dmt bid Summary Item
 * */
class DmtBidSummaryItem(data: DmtBidSummaryItemData) :
    BaseDmtBidSummaryRVAdapterItem<DmtBidSummaryItemData>(DmtBidsSummaryRVAdapterItemType.Summary, data)


/**
 * Dmt Bid Progress Item
 */
class DmtBidSummaryProgressItem(data: DmtBidSummaryProgressItemData) :
    BaseDmtBidSummaryRVAdapterItem<DmtBidSummaryProgressItemData>(DmtBidsSummaryRVAdapterItemType.Progress, data)

/**
 * Dmt Bid Timeout Item
 */
class BulkBidTimeoutItem(data: HomeBidsTimeOutItemData) :
    BaseDmtBidSummaryRVAdapterItem<HomeBidsTimeOutItemData>(DmtBidsSummaryRVAdapterItemType.Timeout, data)
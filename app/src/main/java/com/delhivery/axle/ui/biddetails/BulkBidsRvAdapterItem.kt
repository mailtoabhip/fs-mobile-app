package com.delhivery.axle.ui.biddetails

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.BulkBidSummaryProgressItemData
import com.delhivery.axle.data.home.bids.HomeBidsTimeOutItemData


enum class BulkBidsSummaryRVAdapterItemType(val typeId: Int) {

    Summary(0),
    Progress(1),
    Timeout(2);

    companion object{
        fun byTypeId(typeId: Int) = values().filter { typeId === it.typeId }.firstOrNull()
    }
}

abstract class BaseBulkBidSummaryRVAdapterItem<D: BaseKeyTypeModel<String>>(
        val type: BulkBidsSummaryRVAdapterItemType,
        val data: D
) : BaseKeyTypeModel<String>(){
    override fun key() = data.key()
}

/**
 * Bulk bid Summary Item
 * */
class BulkBidSummaryItem(data: BulkBidSummaryItemData) :
        BaseBulkBidSummaryRVAdapterItem<BulkBidSummaryItemData>(BulkBidsSummaryRVAdapterItemType.Summary, data)


/**
 * BulkBid Progress Item
 */
class BulkBidSummaryProgressItem(data: BulkBidSummaryProgressItemData) :
        BaseBulkBidSummaryRVAdapterItem<BulkBidSummaryProgressItemData>(BulkBidsSummaryRVAdapterItemType.Progress, data)

/**
 * BulkBid Timeout Item
 */
class BulkBidTimeoutItem(data: HomeBidsTimeOutItemData) :
        BaseBulkBidSummaryRVAdapterItem<HomeBidsTimeOutItemData>(BulkBidsSummaryRVAdapterItemType.Timeout, data)

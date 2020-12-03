package com.delhivery.axle.ui.ledger

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.loads.HomeLoadsProgressItemData
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutItemData
import com.delhivery.axle.data.home.loads.HomeLoadsWarningItemData
import com.delhivery.axle.data.home.trips.HomeTripsSearchItemData
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedProgressItemData
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Search
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Ledger
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Warning
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Progress
import com.delhivery.axle.ui.ledger.ConsolidatedPageRVAdapterItemType.Timeout


enum class ConsolidatedPageRVAdapterItemType(val typeId: Int){
    Header(0),
    Search(1),
    Ledger(3),
    Warning(4),
    Progress(5),
    Timeout(6);

    companion object{
        fun byTypeId(typeId: Int) = values().filter { typeId === it.typeId }.firstOrNull()
    }

}
abstract class BaseConsolidatedPageRVAdapterItem< D: BaseKeyTypeModel<String>>(
        val type: ConsolidatedPageRVAdapterItemType,
        val data: D
) : BaseKeyTypeModel<String>(){
    override fun key() = data.key()
}

/**
 * Consolidated Page Ledger request Item
 * */
class ConsolidatedPageLedgerItem(data: ConsolidatedLedgerItemData) :
        BaseConsolidatedPageRVAdapterItem<ConsolidatedLedgerItemData>(Ledger,data)

/**
 * Consolidated Page Warning request Item
 * */
class ConsolidatedPageWarningItem(data: HomeLoadsWarningItemData) :
        BaseConsolidatedPageRVAdapterItem<HomeLoadsWarningItemData>(Warning,data)

/**
 * Consolidated Page Progress request Item
 * */
class ConsolidatedPageProgressItem(data: ConsolidatedProgressItemData) :
        BaseConsolidatedPageRVAdapterItem<ConsolidatedProgressItemData>(Progress,data)

/**
 * Consolidated Page Timeout request Item
 * */
class ConsolidatedPageTimeoutItem(data: HomeLoadsTimeOutItemData) :
        BaseConsolidatedPageRVAdapterItem<HomeLoadsTimeOutItemData>(Timeout,data)

/**
 * Consolidated Page Search request Item
 * */
class ConsolidatedPageSearchItem(data: HomeTripsSearchItemData) :
        BaseConsolidatedPageRVAdapterItem<HomeTripsSearchItemData>(Search,data)
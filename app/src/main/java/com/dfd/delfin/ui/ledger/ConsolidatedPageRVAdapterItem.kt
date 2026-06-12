package com.dfd.delfin.ui.ledger

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.loads.HomeLoadsTimeOutItemData
import com.dfd.delfin.data.home.loads.HomeLoadsWarningItemData
import com.dfd.delfin.data.home.trips.HomeTripsSearchItemData
import com.dfd.delfin.data.ledger.ConsolidatedLedgerItemData
import com.dfd.delfin.data.ledger.ConsolidatedProgressItemData
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Search
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Ledger
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Warning
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Progress
import com.dfd.delfin.ui.ledger.ConsolidatedPageRVAdapterItemType.Timeout


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
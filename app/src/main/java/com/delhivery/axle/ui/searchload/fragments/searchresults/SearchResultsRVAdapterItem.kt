package com.delhivery.axle.ui.searchload.fragments.searchresults

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.HomeBidsSearchSpinnerItemData
import com.delhivery.axle.data.home.bids.HomeBidsWarningItemData
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.*

/**
 * RV item type for [SearchLoadsRVAdapter]
 */
enum class SearchResultsRVAdapterItemType(val typeId: Int) {
  Request(0),
  Contracts(1),
  SearchSpinner(2),
  Warning(3);

  companion object {
    /**
     * Get [SearchResultsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home search type adapter item
 */
abstract class BaseSearchLoadsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchResultsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search request item
 */
class SearchLoadsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Search contracts item
 */
class SearchContractsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsRequestItemData>(Contracts, data)

/**
 * Search load screen dummy view
 */
class SearchLoadsSearchSpinnerItem(data: HomeBidsSearchSpinnerItemData = HomeBidsSearchSpinnerItemData()) :
    BaseSearchLoadsRVAdapterItem<HomeBidsSearchSpinnerItemData>(SearchSpinner, data)

/**
 * Search warning item
 */
class SearchLoadsWarningItem(data: HomeBidsWarningItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsWarningItemData>(Warning, data)
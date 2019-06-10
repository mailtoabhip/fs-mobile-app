package com.delhivery.orion.ui.searchload.fragments.searchresults

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.bids.HomeBidsSearchSpinnerItemData
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner

enum class SearchResultsRVAdapterItemType(val typeId: Int) {
  Request(0),
  SearchSpinner(1);

  companion object {
    /**
     * Get [SearchResultsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseSearchLoadsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchResultsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Bid request item
 */
class SearchLoadsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Search load screen dummy view
 */
class SearchLoadsSearchSpinnerItem(data: HomeBidsSearchSpinnerItemData = HomeBidsSearchSpinnerItemData()) :
    BaseSearchLoadsRVAdapterItem<HomeBidsSearchSpinnerItemData>(SearchSpinner, data)
package com.delhivery.orion.ui.searchload.fragments.searchresults

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.bids.HomeBidsSearchItemData
import com.delhivery.orion.data.home.bids.HomeBidsSearchSpinnerItemData
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Search
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner

enum class SearchResultsRVAdapterItemType(val typeId: Int) {
  Search(0),
  Request(1),
  SearchSpinner(2);

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
abstract class BaseSearchResultsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchResultsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search item with live load requests
 */
class HomeBidsSearchItem(
  data: HomeBidsSearchItemData = HomeBidsSearchItemData(
      0
  )
) : BaseSearchResultsRVAdapterItem<HomeBidsSearchItemData>(Search, data)

/**
 * Bid request item
 */
class HomeBidsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchResultsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Search load screen dummy view
 */
class HomeBidsSearchSpinnerItem(data: HomeBidsSearchSpinnerItemData = HomeBidsSearchSpinnerItemData()) :
    BaseSearchResultsRVAdapterItem<HomeBidsSearchSpinnerItemData>(SearchSpinner, data)
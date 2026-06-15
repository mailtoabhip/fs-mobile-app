package com.dfd.delfin.ui.searchtrip

import com.dfd.delfin.api.request.SearchRequest
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.search.SearchProgressItemData
import com.dfd.delfin.data.search.SearchTimeOutItemData
import com.dfd.delfin.data.search.SearchWarningItemData
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.Progress
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.Search
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.Searched
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.Timeout
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.TripItem
import com.dfd.delfin.ui.searchtrip.SearchRVAdapterItemType.Warning

/**
 * RV item type for [SearchRVAdapter]
 */
enum class SearchRVAdapterItemType(val typeId: Int) {
  Search(0),
  Searched(1),
  TripItem(2),
  Warning(3),
  Progress(4),
  Timeout(5);

  companion object {
    /**
     * Get [SearchRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseSearchRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search query item
 */
class SearchQueryItem(data: SearchRequest) :
    BaseSearchRVAdapterItem<SearchRequest>(Search, data)

/**
 * Search queried item
 */
class SearchedQueryItem(data: SearchRequest) :
    BaseSearchRVAdapterItem<SearchRequest>(Searched, data)

/**
 * Search item
 */
class SearchDataItem(data: HomeTripsItemData) :
    BaseSearchRVAdapterItem<HomeTripsItemData>(TripItem, data)

/**
 * Warning/action item
 */
class SearchWarningItem(data: SearchWarningItemData) :
    BaseSearchRVAdapterItem<SearchWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class SearchTimeoutItem(data: SearchTimeOutItemData) :
    BaseSearchRVAdapterItem<SearchTimeOutItemData>(Timeout, data)

/**
 * Inline progress item
 */
class SearchProgressItem(data: SearchProgressItemData = SearchProgressItemData()) :
    BaseSearchRVAdapterItem<SearchProgressItemData>(Progress, data)
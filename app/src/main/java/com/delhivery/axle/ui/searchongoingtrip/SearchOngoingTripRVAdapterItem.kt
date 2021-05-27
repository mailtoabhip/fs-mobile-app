package com.delhivery.axle.ui.searchongoingtrip

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.search.SearchProgressItemData
import com.delhivery.axle.data.search.SearchTimeOutItemData
import com.delhivery.axle.data.search.SearchWarningItemData
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Progress
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Timeout
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.TripItem
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripRVAdapterItemType.Warning

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

/**
 * RV item type for [SearchOngoingTripRVAdapter]
 */
enum class SearchOngoingTripRVAdapterItemType(val typeId: Int) {
  TripItem(0),
  Warning(1),
  Progress(2),
  Timeout(3);

  companion object {
    /**
     * Get [SearchOngoingTripRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseSearchOngoingTripRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchOngoingTripRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search item
 */
class SearchDataItem(data: HomeTripsItemData) :
    BaseSearchOngoingTripRVAdapterItem<HomeTripsItemData>(TripItem, data)

/**
 * Warning/action item
 */
class SearchWarningItem(data: SearchWarningItemData) :
    BaseSearchOngoingTripRVAdapterItem<SearchWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class SearchTimeoutItem(data: SearchTimeOutItemData) :
    BaseSearchOngoingTripRVAdapterItem<SearchTimeOutItemData>(Timeout, data)

/**
 * Inline progress item
 */
class SearchProgressItem(data: SearchProgressItemData = SearchProgressItemData()) :
    BaseSearchOngoingTripRVAdapterItem<SearchProgressItemData>(Progress, data)
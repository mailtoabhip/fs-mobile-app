package com.delhivery.axle.ui.home.fragments.trips

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trips.HomeTripsHeaderItemData
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsProgressItemData
import com.delhivery.axle.data.home.trips.HomeTripsSearchItemData
import com.delhivery.axle.data.home.trips.HomeTripsTimeOutItemData
import com.delhivery.axle.data.home.trips.HomeTripsWarningItemData
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.CompletedTrip
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Warning

/**
 * RV item type for [HomeTripsRVAdapter]
 */
enum class HomeTripsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  TripItem(2),
  CompletedTrip(3),
  Progress(4),
  Warning(5),
  Timeout(6);

  companion object {
    /**
     * Get [HomeTripsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base home trips type adapter item
 */
abstract class BaseHomeTripsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: HomeTripsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search item
 */
class HomeTripsSearchItem(data: HomeTripsSearchItemData = HomeTripsSearchItemData()) :
    BaseHomeTripsRVAdapterItem<HomeTripsSearchItemData>(Search, data)

/**
 * Trip item
 */
class HomeTripsItem(data: HomeTripsItemData) :
    BaseHomeTripsRVAdapterItem<HomeTripsItemData>(TripItem, data)

/**
 * Completed Trip item
 */
class HomeCompletedTripItem(data: HomeTripsItemData) :
    BaseHomeTripsRVAdapterItem<HomeTripsItemData>(CompletedTrip, data)

/**
 * Inline progress item
 */
class HomeTripsProgressItem(data: HomeTripsProgressItemData = HomeTripsProgressItemData()) :
    BaseHomeTripsRVAdapterItem<HomeTripsProgressItemData>(Progress, data)

/**
 * Trip header items
 */
class HomeTripsHeaderItem(
  data: HomeTripsHeaderItemData = HomeTripsHeaderItemData()
) : BaseHomeTripsRVAdapterItem<HomeTripsHeaderItemData>(Header, data)

/**
 * Warning/action item
 */
class HomeTripsWarningItem(data: HomeTripsWarningItemData) :
    BaseHomeTripsRVAdapterItem<HomeTripsWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class HomeTripsTimeoutItem(data: HomeTripsTimeOutItemData) :
    BaseHomeTripsRVAdapterItem<HomeTripsTimeOutItemData>(Timeout, data)
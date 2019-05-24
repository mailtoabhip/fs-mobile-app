package com.delhivery.orion.ui.home.fragments.trips

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.trips.HomeTripsItemData
import com.delhivery.orion.data.home.trips.HomeTripsSearchItemData
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsRVAdapterItemType.TripItem

enum class HomeTripsRVAdapterItemType(val typeId: Int) {
  Search(0),
  TripItem(1);

  companion object {
    /**
     * Get [HomeTripsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) =
      HomeTripsRVAdapterItemType.values().filter { typeId == it.typeId }.firstOrNull()
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
package com.delhivery.axle.ui.home.fragments.loads

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.HomeLoadsFilterItemData
import com.delhivery.axle.data.home.loads.HomeLoadsInfoItemData
import com.delhivery.axle.data.home.loads.HomeLoadsProgressItemData
import com.delhivery.axle.data.home.loads.HomeLoadsSearchItemData
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutItemData
import com.delhivery.axle.data.home.loads.HomeLoadsWarningItemData
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Filters
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Info
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Warning

/**
 * RV item type for [HomeLoadsRVAdapter]
 */
enum class HomeLoadsRVAdapterItemType(val typeId: Int) {
  Request(0),
  Progress(1),
  Search(2),
  Warning(3),
  Timeout(4),
  Info(5),
  Filters(6);

  companion object {
    /**
     * Get [HomeLoadsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home loads type adapter item
 */
abstract class BaseHomeLoadsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: HomeLoadsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Load request item
 */
class HomeLoadsRequestItem(data: HomeBidsRequestItemData) :
    BaseHomeLoadsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Inline progress item
 */
class HomeLoadsProgressItem(
  data: HomeLoadsProgressItemData = HomeLoadsProgressItemData()
) : BaseHomeLoadsRVAdapterItem<HomeLoadsProgressItemData>(Progress, data)

/**
 * Search item with live load requests
 */
class HomeLoadsSearchItem(
  data: HomeLoadsSearchItemData = HomeLoadsSearchItemData()
) : BaseHomeLoadsRVAdapterItem<HomeLoadsSearchItemData>(Search, data)

/**
 * Warning/action item
 */
class HomeLoadsWarningItem(data: HomeLoadsWarningItemData) :
    BaseHomeLoadsRVAdapterItem<HomeLoadsWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class HomeLoadsTimeoutItem(data: HomeLoadsTimeOutItemData) :
    BaseHomeLoadsRVAdapterItem<HomeLoadsTimeOutItemData>(Timeout, data)

/**
 * Inline progress item
 */
class HomeLoadsInfoItem(
  data: HomeLoadsInfoItemData = HomeLoadsInfoItemData(
      "These are all the recommended loads. To find more relevant loads Search here!",
      "To get more relevant loads - change your \n preferences here!"
  )
) : BaseHomeLoadsRVAdapterItem<HomeLoadsInfoItemData>(Info, data)

/**
 * Filter item
 */
class HomeLoadsFilterItem(data: HomeLoadsFilterItemData) :
    BaseHomeLoadsRVAdapterItem<HomeLoadsFilterItemData>(Filters, data)
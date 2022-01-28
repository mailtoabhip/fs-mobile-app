package com.delhivery.axle.ui.home.fragments.loads

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.*
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.*

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
  MoreInfo(6),
  Filters(7),
  Count(8),
  Banners(9),
  Priority(10);

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
 * Inventory banner item
 */
class HomeLoadsAddTruckItem(data: HomeLoadsAddTruckItemData = HomeLoadsAddTruckItemData()) :
  BaseHomeLoadsRVAdapterItem<HomeLoadsAddTruckItemData>(Banners, data)


/**
 * Priority acccess banner item
 */
class HomeLoadsTruckPriorityAccessItem(data: HomeLoadsTruckPriorityAccessItemData = HomeLoadsTruckPriorityAccessItemData()) :
  BaseHomeLoadsRVAdapterItem<HomeLoadsTruckPriorityAccessItemData>(Priority, data)

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
 * Inline Info item
 */
class HomeLoadsInfoItem(
  data: HomeLoadsInfoItemData = HomeLoadsInfoItemData(
      "These are all the recommended loads. To find more relevant loads Click here!"
  )
) : BaseHomeLoadsRVAdapterItem<HomeLoadsInfoItemData>(Info, data)


/**
 * Inline more info item
 */
class HomeLoadsMoreInfoItem(
  data: HomeLoadsMoreInfoItemData = HomeLoadsMoreInfoItemData(
    "To get more relevant loads - change your \n preferences here!"
  )
) : BaseHomeLoadsRVAdapterItem<HomeLoadsMoreInfoItemData>(MoreInfo, data)

/**
 * Filter item
 */
class HomeLoadsFilterItem(
  data: HomeLoadsFilterItemData = HomeLoadsFilterItemData(
      false
  )
) : BaseHomeLoadsRVAdapterItem<HomeLoadsFilterItemData>(Filters, data)

/**
 * Summary Item
 */
class HomeLoadsSummaryItem(
  data :HomeLoadsSummaryItemData
):BaseHomeLoadsRVAdapterItem<HomeLoadsSummaryItemData>(Count,data )
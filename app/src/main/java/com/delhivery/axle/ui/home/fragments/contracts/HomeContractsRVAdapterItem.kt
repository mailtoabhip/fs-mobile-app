package com.delhivery.axle.ui.home.fragments.contracts

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.HomeContractsFilterItemData
import com.delhivery.axle.data.home.contracts.HomeContractsProgressItemData
import com.delhivery.axle.data.home.contracts.HomeContractsTimeOutItemData
import com.delhivery.axle.data.home.contracts.HomeContractsWarningItemData
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Contracts
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.FilterInfo
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.FilterToggle
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Filters
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Warning

/*
* RV item type for [HomeContractsRVAdapter]
*/
enum class HomeContractsRVAdapterItemType(val typeId: Int) {
  Contracts(0),
  Search(1),
  Progress(2),
  Warning(3),
  Timeout(4),
  Filters(5),
  FilterToggle(6),
  FilterInfo(7);

  companion object {
    /**
     * Get [HomeContractsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = HomeContractsRVAdapterItemType.values()
      .filter { typeId == it.typeId }.firstOrNull()
  }
}
/**
 * Base Home Contracts type adapter item
 */
abstract class BaseHomeContractsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: HomeContractsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Load request item
 */
class HomeContractsRequestItem(data: HomeBidsRequestItemData) :
  BaseHomeContractsRVAdapterItem<HomeBidsRequestItemData>(Contracts, data)

/**
 * Inline progress item
 */
class HomeContractsProgressItem(
  data: HomeContractsProgressItemData = HomeContractsProgressItemData()
) : BaseHomeContractsRVAdapterItem<HomeContractsProgressItemData>(Progress, data)

/**
 * Search item with live contract requests
 */
class HomeContractsSearchItem(
  data: HomeContractsSearchItemData = HomeContractsSearchItemData()
) : BaseHomeContractsRVAdapterItem<HomeContractsSearchItemData>(Search, data)

/**
 * Warning/action item
 */
class HomeContractsWarningItem(data: HomeContractsWarningItemData) :
  BaseHomeContractsRVAdapterItem<HomeContractsWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class HomeContractsTimeoutItem(data: HomeContractsTimeOutItemData) :
  BaseHomeContractsRVAdapterItem<HomeContractsTimeOutItemData>(Timeout, data)

/**
 * Filter item
 */
class HomeContractsFilterItem(
  data: HomeContractsFilterItemData = HomeContractsFilterItemData("",
    false, 0,0,0,""
  )
) : BaseHomeContractsRVAdapterItem<HomeContractsFilterItemData>(Filters, data)
/**
 * Filter option toggle item
 */
class FilterToggleItem(
  data: HomeContractsFilterItemData = HomeContractsFilterItemData("",
    false, 0,0,0,""
  )
): BaseHomeContractsRVAdapterItem<HomeContractsFilterItemData>(FilterToggle,data)

/**
 * Filter option info item
 */
class FilterInfoItem(
  data: HomeContractsFilterItemData = HomeContractsFilterItemData("Info",
    false, 0,0,0,""
  )
): BaseHomeContractsRVAdapterItem<HomeContractsFilterItemData>(FilterInfo,data)
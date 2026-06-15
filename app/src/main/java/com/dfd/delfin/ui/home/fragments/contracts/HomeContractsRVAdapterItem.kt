package com.dfd.delfin.ui.home.fragments.contracts

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.data.home.contracts.HomeContractsFilterItemData
import com.dfd.delfin.data.home.contracts.HomeContractsIntracityFilterItemData
import com.dfd.delfin.data.home.contracts.HomeContractsProgressItemData
import com.dfd.delfin.data.home.contracts.HomeContractsTimeOutItemData
import com.dfd.delfin.data.home.contracts.HomeContractsWarningItemData
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Contracts
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Filters
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.IntracityFilters
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Warning
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsRVAdapterItemType.Search

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
  IntracityFilters(6),
  IntracityContracts(7);  // New type for intracity contracts

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
  data: HomeContractsFilterItemData = HomeContractsFilterItemData(
    "", 0,0,0,"",false
  )
) : BaseHomeContractsRVAdapterItem<HomeContractsFilterItemData>(Filters, data)

/**
 * Filter intracity item
 */
class HomeContractsIntracityFilterItem(
  data: HomeContractsIntracityFilterItemData = HomeContractsIntracityFilterItemData(
    ""
  )
) : BaseHomeContractsRVAdapterItem<HomeContractsIntracityFilterItemData>(IntracityFilters, data)

package com.dfd.delfin.ui.home.fragments.bids

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.bids.HomeBidsHeaderItemData
import com.dfd.delfin.data.home.bids.HomeBidsProgressItemData
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.data.home.bids.HomeBidsSearchItemData
import com.dfd.delfin.data.home.bids.HomeBidsTimeOutItemData
import com.dfd.delfin.data.home.bids.HomeBidsWarningItemData
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Contracts
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

/**
 * RV item type for [HomeBidsRVAdapter]
 */
enum class HomeBidsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  Request(2),
  Warning(3),
  Progress(4),
  Timeout(5),
  Contracts(6),
  IntracityBids(7),
  MarketplaceBids(8);

  companion object {
    /**
     * Get [HomeBidsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home bids type adapter item
 */
abstract class BaseHomeBidsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: HomeBidsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Header item with my bids and confirmed bids
 */
class HomeBidsHeaderItem(
  data: HomeBidsHeaderItemData = HomeBidsHeaderItemData()
) : BaseHomeBidsRVAdapterItem<HomeBidsHeaderItemData>(Header, data)

/**
 * Search item with live load requests
 */
class HomeBidsSearchItem(
  data: HomeBidsSearchItemData = HomeBidsSearchItemData()
) : BaseHomeBidsRVAdapterItem<HomeBidsSearchItemData>(Search, data)

/**
 * Bid request item
 */
class HomeBidsRequestItem(data: HomeBidsRequestItemData) :
    BaseHomeBidsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Warning/action item
 */
class HomeBidsWarningItem(data: HomeBidsWarningItemData) :
    BaseHomeBidsRVAdapterItem<HomeBidsWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class HomeBidsTimeoutItem(data: HomeBidsTimeOutItemData) :
    BaseHomeBidsRVAdapterItem<HomeBidsTimeOutItemData>(Timeout, data)

/**
 * Inline progress item
 */
class HomeBidsProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseHomeBidsRVAdapterItem<HomeBidsProgressItemData>(Progress, data)

//No more in use as of now
/**
 * Contracts Bid request item
 */
class HomeContractsBidsRequestItem(data: HomeBidsRequestItemData) :
  BaseHomeBidsRVAdapterItem<HomeBidsRequestItemData>(Contracts, data)
package com.delhivery.axle.ui.home.fragments.bids

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.axle.data.home.bids.HomeBidsProgressItemData
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.HomeBidsSearchItemData
import com.delhivery.axle.data.home.bids.HomeBidsTimeOutItemData
import com.delhivery.axle.data.home.bids.HomeBidsWarningItemData
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

/**
 * RV item type for [HomeBidsRVAdapter]
 */
enum class HomeBidsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  Request(2),
  Warning(3),
  Progress(4),
  Timeout(5);

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
package com.delhivery.orion.ui.home.fragments.bids

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.bids.HomeBidsHeaderItemData
import com.delhivery.orion.data.home.bids.HomeBidsProgressItemData
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.bids.HomeBidsSearchItemData
import com.delhivery.orion.data.home.bids.HomeBidsWarningItemData
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

enum class HomeBidsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  Request(2),
  Warning(3),
  Progress(4);

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
 * //TODO: Add lost bid data
 * Header item with my bids and confirmed bids
 */
class HomeBidsHeaderItem(
  data: HomeBidsHeaderItemData = HomeBidsHeaderItemData(
      0, 0
  )
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
 * Inline progress item
 */
class HomeBidsProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseHomeBidsRVAdapterItem<HomeBidsProgressItemData>(Progress, data)
package com.delhivery.orion.ui.home.fragments.bids

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.HomeBidsHeaderItemData
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.data.home.HomeBidsSearchItemData
import com.delhivery.orion.data.home.HomeBidsSearchSpinnerItemData
import com.delhivery.orion.data.home.HomeBidsWarningItemData
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Header
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Request
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.SearchSpinner
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterItemType.Warning

enum class HomeBidsRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  Request(2),
  Warning(3),
  SearchSpinner(4);

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
class HomeBidsHeaderItem(data: HomeBidsHeaderItemData = HomeBidsHeaderItemData(0, 0)) :
    BaseHomeBidsRVAdapterItem<HomeBidsHeaderItemData>(Header, data)

/**
 * Search item with live load requests
 */
class HomeBidsSearchItem(data: HomeBidsSearchItemData = HomeBidsSearchItemData(0)) :
    BaseHomeBidsRVAdapterItem<HomeBidsSearchItemData>(Search, data)

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
 * Search load screen dummy view
 */
class HomeBidsSearchSpinnerItem(data: HomeBidsSearchSpinnerItemData = HomeBidsSearchSpinnerItemData()) :
    BaseHomeBidsRVAdapterItem<HomeBidsSearchSpinnerItemData>(SearchSpinner, data)
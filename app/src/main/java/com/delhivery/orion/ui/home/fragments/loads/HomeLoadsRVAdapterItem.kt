package com.delhivery.orion.ui.home.fragments.loads

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.data.home.loads.HomeLoadsInfoItemData
import com.delhivery.orion.data.home.loads.HomeLoadsProgressItemData
import com.delhivery.orion.data.home.loads.HomeLoadsSearchItemData
import com.delhivery.orion.data.home.loads.HomeLoadsWarningItemData
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Info
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Search
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Warning

enum class HomeLoadsRVAdapterItemType(val typeId: Int) {
  Request(0),
  Progress(1),
  Search(2),
  Warning(3),
  Info(4);

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
 * Inline progress item
 */
class HomeLoadsInfoItem(
  data: HomeLoadsInfoItemData = HomeLoadsInfoItemData(
      "These are all the recommended loads. To find more relevant loads Search here!!",
      "To get more relevant loads - change your \n preferences here!!"
  )
) : BaseHomeLoadsRVAdapterItem<HomeLoadsInfoItemData>(Info, data)
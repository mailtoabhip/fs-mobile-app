package com.delhivery.orion.ui.home.fragments.loads

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.bids.HomeBidsProgressItemData
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Progress
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsRVAdapterItemType.Request

enum class HomeLoadsRVAdapterItemType(val typeId: Int) {
  Request(0),
  Progress(1);

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
class HomeLoadsProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseHomeLoadsRVAdapterItem<HomeBidsProgressItemData>(Progress, data)

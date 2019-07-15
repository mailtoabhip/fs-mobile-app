package com.delhivery.orion.ui.selectroute.fragments.routeslist

import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.home.routes.RouteModel
import com.delhivery.orion.data.home.routes.RoutesAddItemData
import com.delhivery.orion.data.home.routes.RoutesProgressItemData
import com.delhivery.orion.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.AddAction
import com.delhivery.orion.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Progress
import com.delhivery.orion.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Request

enum class RoutesRVAdapterItemType(val typeId: Int) {
  Request(0),
  Progress(1),
  AddAction(2);

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
abstract class RoutesRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: RoutesRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search item with live load requests
 */
class RoutesRequestItem(
  data: RouteModel
) : RoutesRVAdapterItem<RouteModel>(Request, data)

/**
 * Bid request item
 */
class RoutesProgressItem(data: RoutesProgressItemData = RoutesProgressItemData()) :
    RoutesRVAdapterItem<RoutesProgressItemData>(Progress, data)

/**
 * Inline progress item
 */
class RoutesAddItem(data: RoutesAddItemData = RoutesAddItemData()) :
    RoutesRVAdapterItem<RoutesAddItemData>(AddAction, data)
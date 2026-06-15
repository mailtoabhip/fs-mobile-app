package com.dfd.delfin.ui.selectroute.fragments.routeslist

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.routes.RouteModel
import com.dfd.delfin.data.home.routes.RoutesAddItemData
import com.dfd.delfin.data.home.routes.RoutesProgressItemData
import com.dfd.delfin.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.AddAction
import com.dfd.delfin.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Progress
import com.dfd.delfin.ui.selectroute.fragments.routeslist.RoutesRVAdapterItemType.Request

/**
 * RV item type for [RoutesRVAdapter]
 */
enum class RoutesRVAdapterItemType(val typeId: Int) {
  Request(0),
  Progress(1),
  AddAction(2);

  companion object {
    /**
     * Get [RoutesRVAdapter] by typeId
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
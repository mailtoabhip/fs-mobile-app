package com.delhivery.axle.ui.userroutes

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsProgressItemData
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.data.userroutes.UserRoutesWarningItemData
import com.delhivery.axle.ui.userroutes.UserRoutesRVAdapterItemType.*

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

/**
 * RV item type for [UserRoutesRVAdapter]
 */
enum class UserRoutesRVAdapterItemType(val typeId: Int) {
  Route(0),
  Progress(1),
  Warning(2);

  companion object {
    /**
     * Get [UserRoutesRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base user route type adapter item
 */
abstract class BaseUserRouteRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: UserRoutesRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * User route item
 */
class UserRouteItem(data: RouteModel) :
    BaseUserRouteRVAdapterItem<RouteModel>(Route, data)

/**
 * User route progress item
 */
class UserRouteProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseUserRouteRVAdapterItem<HomeBidsProgressItemData>(Progress, data)

/**
 * User route warning item
 */
class UserRouteWarningItem(data: UserRoutesWarningItemData) :
      BaseUserRouteRVAdapterItem<UserRoutesWarningItemData>(Warning, data)
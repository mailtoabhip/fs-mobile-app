package com.delhivery.axle.ui.selectroute

enum class SelectRouteFlowType(
  val typeId: Int
) {
  AddNewRoute(0),
  EditRoute(1),
  DeleteRoute(2);

  companion object {
    /**
     * Get [SelectRouteFlowType] by type id
     */
    fun byTypeId(typeId: Int) =
      SelectRouteFlowType.values().filter { it.typeId == typeId }.firstOrNull() ?: AddNewRoute
  }
}
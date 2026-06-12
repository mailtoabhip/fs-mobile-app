package com.dfd.delfin.data.home.routes

import com.dfd.delfin.data.BaseKeyTypeModel

data class RoutesAddItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = RoutesAddItemDataKey
}

private const val RoutesAddItemDataKey = "add_route"

/* actions */
const val RoutesAction_AddRoute = "add_route"
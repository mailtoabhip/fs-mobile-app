package com.dfd.delfin.data.home.routes

import com.dfd.delfin.data.BaseKeyTypeModel

data class RoutesProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = RoutesProgressItemDataKey
}

private const val RoutesProgressItemDataKey = "progress"
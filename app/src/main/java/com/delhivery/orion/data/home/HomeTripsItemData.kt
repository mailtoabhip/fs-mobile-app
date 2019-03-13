package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel
import java.util.UUID

data class HomeTripsItemData(
  val id: String = UUID.randomUUID().toString()
) : BaseKeyTypeModel<String>() {
  override fun key() = id
}
package com.delhivery.orion.data.home

import com.delhivery.orion.data.BaseKeyTypeModel
import java.util.UUID

data class HomeBidsRequestItemData(
  val amount: Int = 0,
  val id: String = UUID.randomUUID().toString()
) : BaseKeyTypeModel<String>() {
  override fun key() = id
}

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"
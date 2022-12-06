package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsProgressItemDataKey
}

private const val HomeContractsProgressItemDataKey = "progress"
package com.dfd.delfin.data.home.contracts

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeContractsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsProgressItemDataKey
}

private const val HomeContractsProgressItemDataKey = "progress"
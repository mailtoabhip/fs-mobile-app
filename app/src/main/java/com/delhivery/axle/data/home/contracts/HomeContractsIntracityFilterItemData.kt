package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsIntracityFilterItemData(
  var filterType: String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsIntracityFilterItemDataPrefix
}

/* unique key for filter */
const val HomeContractsIntracityFilterItemDataPrefix = "intracity_filter_"

/* actions */
const val HomeContractsIntracityFilterFixed = "filter_intracity_fixed"
const val HomeContractsIntracityFilterFlexible = "filter_intracity_flexible"
const val HomeContractsIntracityFilterAll = "filter_intracity_all"
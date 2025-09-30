package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsFilterItemData(
  var filterType: String, var expressCount:Int, var nonExpressCount:Int, var intraCity:Int, var userDemandType:String,var userContractDemand:Boolean
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeContractsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeContractsFilterExpress = "filter_express"
// Removed: const val HomeContractsFilterNonExpress = "filter_non_express" - merged into express filter
const val HomeContractsFilterIntracity = "filter_intracity"

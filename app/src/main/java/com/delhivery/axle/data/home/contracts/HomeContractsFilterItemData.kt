package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsFilterItemData(
  var actionLabel: Boolean, var expressCount:Int, var nonExpressCount:Int, var userDemandType:String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeContractsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeContractsFilterExpress = "filter_express"
const val HomeContractsFilterNonExpress = "filter_non_express"
const val HomeContractsFilterInfo = "filter_info"
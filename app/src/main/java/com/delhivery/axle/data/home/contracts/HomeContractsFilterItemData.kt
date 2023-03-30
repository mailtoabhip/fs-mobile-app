package com.delhivery.axle.data.home.contracts

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeContractsFilterItemData(
  var itemType:String, var actionLabel: Boolean, var expressCount:Int, var nonExpressCount:Int,var intracityCount:Int, var userDemandType:String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeContractsFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeContractsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeContractsFilterDLVIntercity = "filter_express"
const val HomeContractsFilterCustomerIntercity = "filter_non_express"
const val HomeContractsFilterDLVIntracity="filter_dlv_intracity"
const val HomeContractsFilterInfo = "filter_info"
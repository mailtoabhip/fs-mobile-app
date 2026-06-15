package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsFilterItemData(
  var filterType: String, var dlvIntracityCount:Int, var dlvIntercityCount:Int, var nonDlvCount:Int, var marketplaceCount:Int = 0, var userDemandType:String
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeLoadsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeLoadsFilterAction = "filter"
const val HomeLoadsVehicleFilterAction = "filter_vehicle"
const val HomeLoadDlvIntracity = "filter_dlv_intracity"
const val HomeLoadDlvIntercity = "filter_dlv_intercity"
const val HomeLoadNonDlv = "filter_non_dlv"
const val HomeLoadMarketplace = "filter_marketplace"
package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsMarketPlaceInfoItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsMarketPlaceInfoItemDataKey
}

private const val HomeLoadsMarketPlaceInfoItemDataKey = "marketPlace_info"



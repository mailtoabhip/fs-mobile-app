package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsMarketPlaceInfoItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsMarketPlaceInfoItemDataKey
}

private const val HomeLoadsMarketPlaceInfoItemDataKey = "marketPlace_info"



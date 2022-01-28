package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeLoadsAddTruckItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsAddTruckItemDataKey
}

private const val HomeLoadsAddTruckItemDataKey = "banner"

const val HomeLoadsAddTruckItemDataConfig = 7

//actions
const val HomeLoadsBannerAction =  "click_banner"
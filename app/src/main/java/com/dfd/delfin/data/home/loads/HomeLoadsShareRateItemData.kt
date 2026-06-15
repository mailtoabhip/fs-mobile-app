package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel


data class HomeLoadsShareRateItemData(
  val showing: Boolean = true,
    val title:String,
    val subTitle:String,
    val rate:String
) : BaseKeyTypeModel<String>() {
  override fun key() =HomeLoadsShareRateItemDataKey
}

private const val HomeLoadsShareRateItemDataKey = "share_rate"

//actions
const val HomeLoadsShareRateAction = "click_share_rate"


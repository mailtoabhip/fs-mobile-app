package com.delhivery.axle.data.home.loads

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject


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


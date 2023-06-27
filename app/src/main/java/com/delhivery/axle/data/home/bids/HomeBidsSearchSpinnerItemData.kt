package com.delhivery.axle.data.home.bids

import android.view.View
import com.delhivery.axle.data.BaseKeyTypeModel

data class HomeBidsSearchSpinnerItemData(
  val isIntracity: Boolean = false
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchSpinnerItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchSpinnerItemDataKey = "search_spinner"
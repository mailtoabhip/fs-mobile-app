package com.delhivery.orion.data.home

import android.view.View
import com.delhivery.orion.data.BaseKeyTypeModel

data class HomeBidsSearchSpinnerItemData(
  val visibility: Int = View.INVISIBLE
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeBidsSearchSpinnerItemDataKey
}

/* unique key for diff */
private const val HomeBidsSearchSpinnerItemDataKey = "search_spinner"
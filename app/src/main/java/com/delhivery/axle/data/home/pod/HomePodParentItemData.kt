package com.delhivery.axle.data.home.pod

import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils

class HomePodParentItemData(
  val name: String,
  val podDatas: List<HomePodChildItemData>,
  var expanded: Boolean = false
) : BaseKeyTypeModel<String>() {
  override fun key() = name

  /**
   * @return expanded resource basis [expanded]
   */
  @DrawableRes
  fun expandedResource() = DrawableProviderUtils.expandedRes(expanded)
}

const val HomePodParentAction = "toggle"
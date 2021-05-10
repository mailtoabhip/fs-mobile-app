package com.delhivery.axle.data.tripdetail

import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

data class TripPaymentSummaryItemData (
  var title: String,
  var itemSummary: List<TripPaymentSummaryDetailItemData>,
  var expanded: Boolean = false

) : BaseKeyTypeModel<String>() {

  override fun key() = title

  /**
   * @return expanded resource basis [expanded]
   */
  @DrawableRes
  fun expandedResource() = DrawableProviderUtils.expandedResLedger(expanded)

}
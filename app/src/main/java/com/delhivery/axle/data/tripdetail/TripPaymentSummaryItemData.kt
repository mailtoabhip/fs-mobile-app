package com.delhivery.axle.data.tripdetail

import androidx.annotation.DrawableRes
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

data class TripPaymentSummaryItemData (
  var title: String,
  var itemSummary: List<TripPaymentSummaryDetailItemData>,
  var expanded: Boolean = false,
  var amount: Double ?= 0.0

) : BaseKeyTypeModel<String>() {

  override fun key() = title

  fun totalAmount() = if (itemSummary.isNotEmpty() && title != "Pending Payment/Recovery") {
    var total = 0.0
    for (summary in itemSummary) {
      total += summary.amount
    }
    "₹ ${StringUtils.formatAmount(total)}"
  } else if (title == "Pending Payment/Recovery") {
    "₹ ${StringUtils.formatAmount(amount ?: 0.0)}"
  } else {
    "₹ 0"
  }

  /**
   * @return expanded resource basis [expanded]
   */
  @DrawableRes
  fun expandedResource() = DrawableProviderUtils.expandedRes(expanded)

}

const val TripPaymentSummaryItemAction = "toggle"
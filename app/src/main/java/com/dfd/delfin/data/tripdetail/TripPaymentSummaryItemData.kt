package com.dfd.delfin.data.tripdetail

import androidx.annotation.DrawableRes
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.utils.DrawableProviderUtils
import com.dfd.delfin.utils.StringUtils

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

  fun totalAmount() = if (itemSummary.isNotEmpty() && (title != "Pending Payment" || title != "Pending Recovery")) {
    var total = 0.0
    for (summary in itemSummary) {
      if (title == "Deductions" && summary.title == "Waived Off") {
        total -= summary.amount
      } else {
        total += summary.amount
      }
    }
    "₹ ${StringUtils.formatAmount(total)}"
  } else if (title == "Pending Payment" || title == "Pending Recovery") {
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
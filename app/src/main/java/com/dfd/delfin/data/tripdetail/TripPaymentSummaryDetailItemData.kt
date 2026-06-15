package com.dfd.delfin.data.tripdetail

import androidx.annotation.ColorRes
import com.dfd.delfin.R
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.utils.StringUtils

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

data class TripPaymentSummaryDetailItemData (
  var title: String,
  var amount: Double,
  var subTitle: String ? = "",
  var redirectable: Boolean ?= false,
  var transactionId: String ?= "",
  var eventType: String ?= ""

) : BaseKeyTypeModel<String>() {

  override fun key() = title

  fun formattedAmount() = "₹ ${StringUtils.formatAmount(amount)}"

  /**
   * Amount text color
   */
  @ColorRes
  fun amountTextColor() = if (title == "Waived Off") {
    R.color.bid_placed_green
  } else {
    R.color.sub_heading_black
  }


  /**
  * Required at text color as per promise date
  */
  @ColorRes
  fun titleTextColor() = if (redirectable == true) {
    R.color.link
  } else if (title == "Waived Off") {
    R.color.bid_placed_green
  } else {
    R.color.sub_heading_black
  }

}

const val TripPaymentSummaryDetailItemAction = "tripLink"
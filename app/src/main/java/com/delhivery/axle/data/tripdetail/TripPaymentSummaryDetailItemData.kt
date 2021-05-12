package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.StringUtils

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

data class TripPaymentSummaryDetailItemData (
  var title: String,
  var amount: Double,
  var subTitle: String ? = ""

) : BaseKeyTypeModel<String>() {

  override fun key() = title

  fun formattedAmount() = "₹ ${StringUtils.formatAmount(amount)}"

}
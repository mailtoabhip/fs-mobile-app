package com.dfd.delfin.data.tripdetail

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

data class TripPaymentSummaryProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = TripPaymentSummaryProgressItemDataKey
}

private const val TripPaymentSummaryProgressItemDataKey = "progress"
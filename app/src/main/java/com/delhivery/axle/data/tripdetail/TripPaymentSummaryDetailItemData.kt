package com.delhivery.axle.data.tripdetail

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

}
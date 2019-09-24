package com.delhivery.axle.data.fuelcards

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FuelCardData(
  @SerializedName("pan") val pan: Double,
  @SerializedName("mobile") val mobile: String,
  @SerializedName("trip_id") val tripId: String,
  @SerializedName("wallet_transaction_reference_number") val refNumber: String
) : BaseKeyTypeModel<String>(), Serializable {

  override fun key() = refNumber

}

/* actions */
const val FuelCardAction_ViewDetails = "transaction_details"
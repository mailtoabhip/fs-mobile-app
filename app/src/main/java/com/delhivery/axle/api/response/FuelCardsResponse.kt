package com.delhivery.axle.api.response

import com.delhivery.axle.data.fuelcards.FuelCardData
import com.google.gson.annotations.SerializedName

/**
 * Active fuel cards response
 */
data class FuelCardsResponse(
  @SerializedName("active_cards") val cards: List<FuelCardData>
)
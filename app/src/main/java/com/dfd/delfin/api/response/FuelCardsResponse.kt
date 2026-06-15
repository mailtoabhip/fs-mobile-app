package com.dfd.delfin.api.response

import com.dfd.delfin.data.fuelcards.FuelCardData
import com.google.gson.annotations.SerializedName

/**
 * Active fuel cards response
 */
data class FuelCardsResponse(
  @SerializedName("active_cards") val cards: List<FuelCardData>
)
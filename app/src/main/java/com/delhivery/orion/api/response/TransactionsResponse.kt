package com.delhivery.orion.api.response

import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.google.gson.annotations.SerializedName

data class TransactionsResponse(
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("total") val total: Int,
  @SerializedName("offset") val offset: Int,
  @SerializedName("result") val transactions: List<HomeBidsRequestItemData>
)
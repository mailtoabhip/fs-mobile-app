package com.delhivery.axle.api.response

import com.delhivery.axle.data.yourrewards.YourRewardsItemData
import com.google.gson.annotations.SerializedName

data class GetSupplierRewardsResponse (
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("count") val total: Int,
  @SerializedName("details") val rewardsDetails: List<YourRewardsItemData>)
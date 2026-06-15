package com.dfd.delfin.api.response

import com.dfd.delfin.data.yourrewards.YourRewardsItemData
import com.google.gson.annotations.SerializedName

data class GetSupplierRewardsResponse (
  @SerializedName("has_next") val hasNext: Boolean,
  @SerializedName("count") val total: Int,
  @SerializedName("details") val rewardsDetails: List<YourRewardsItemData>)
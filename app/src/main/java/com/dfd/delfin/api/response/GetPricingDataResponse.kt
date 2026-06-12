package com.dfd.delfin.api.response

import com.dfd.delfin.data.yourrewards.YourRewardsItemData
import com.google.gson.annotations.SerializedName

data class GetPricingDataResponse (
  @SerializedName("offers") val pricingData: List<YourRewardsItemData>)
package com.delhivery.axle.api.response

import com.delhivery.axle.data.yourrewards.YourRewardsItemData
import com.google.gson.annotations.SerializedName

data class GetPricingDataResponse (
  @SerializedName("offers") val pricingData: List<YourRewardsItemData>)
package com.delhivery.axle.data.yourrewards

import com.delhivery.axle.data.BaseKeyTypeModel

data class YourRewardsTimeOutItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = YourRewardsTimeOutItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val YourRewardsTimeOutItemDataKeyPrefix = "timeout_"

/* actions */
const val YourRewardsTimeOutAction = "time_out"
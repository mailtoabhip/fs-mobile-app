package com.delhivery.axle.data.yourrewards

import com.delhivery.axle.data.BaseKeyTypeModel

/**
 * Warning item data for Your Rewards list
 */
data class YourRewardsWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = YourRewardsWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val YourRewardsWarningItemDataKeyPrefix = "warning_"

/* actions */
const val YourRewardsWarningAction_NoRewards = "no_Rewards"
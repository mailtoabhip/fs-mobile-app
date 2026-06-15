package com.dfd.delfin.data.yourrewards

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Progress item data for Your Rewards list
 */
data class YourRewardsProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = YourRewardsProgressItemDataKey
}

private const val YourRewardsProgressItemDataKey = "progress"
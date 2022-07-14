package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.data.yourrewards.YourRewardsItemData
import com.delhivery.axle.data.yourrewards.YourRewardsProgressItemData
import com.delhivery.axle.data.yourrewards.YourRewardsTimeOutItemData
import com.delhivery.axle.data.yourrewards.YourRewardsWarningItemData
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Progress
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.YourRewards
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Warning

/**
 * RV item type for [YourRewardsRVAdapter]
 */
enum class YourRewardsRVAdapterItemType(val typeId: Int) {
  YourRewards(0),
  Progress(1),
  Warning(2),
  Timeout(3);

  companion object {
    /**
     * Get [YourRewardsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base MyTrucks adapter item
 */
abstract class BaseYourRewardsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: YourRewardsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * YourReward data item
 */
class YourRewardsItem(data: YourRewardsItemData) :
  BaseYourRewardsRVAdapterItem<YourRewardsItemData>(YourRewards, data)

/**
 * Inline progress item
 */
class YourRewardsProgressItem(data: YourRewardsProgressItemData = YourRewardsProgressItemData()) :
  BaseYourRewardsRVAdapterItem<YourRewardsProgressItemData>(Progress, data)

/**
 * Warning/action item
 */
class YourRewardsWarningItem(data: YourRewardsWarningItemData) :
  BaseYourRewardsRVAdapterItem<YourRewardsWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class YourRewardsTimeoutItem(data: YourRewardsTimeOutItemData) :
  BaseYourRewardsRVAdapterItem<YourRewardsTimeOutItemData>(Timeout, data)
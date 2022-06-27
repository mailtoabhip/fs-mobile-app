package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import com.delhivery.axle.data.yourrewards.YourRewardsTimeOutAction
import com.delhivery.axle.data.yourrewards.YourRewardsWarningAction_NoRewards
import com.delhivery.axle.data.yourrewards.YourRewardsWarningItemData

/**
 * No Rates warning item
 */
val YourRewardsWarningItem_NoRewards = YourRewardsWarningItem(
    YourRewardsWarningItemData(
        "No Rate Shared",
        "",
        "", YourRewardsWarningAction_NoRewards
    )
)

/**
 * Rates timeout item
 */
val YourRewardsWarningItem_TimeOut = YourRewardsWarningItem(
    YourRewardsWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", YourRewardsTimeOutAction
    )
)


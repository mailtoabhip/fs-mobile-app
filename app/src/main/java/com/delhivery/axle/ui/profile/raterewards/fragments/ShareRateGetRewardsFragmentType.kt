package com.delhivery.axle.ui.profile.raterewards.fragments

import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsFragment
import com.delhivery.axle.ui.profile.raterewards.fragments.sharerate.ShareRateFragment

enum class ShareRateGetRewardsFragmentType(
    val position: Int,
    val fragment: BaseFragment<*, *>,
    val title: String
) {
    ShareFragment(0, ShareRateFragment._instance, "Share Rate"),
    RewardsFragment( 1, YourRewardsFragment._instance, "Your Rewards");

    companion object {

        /**
         * Get [ShareRateGetRewardsFragmentType] by position
         */
        fun pos(position: Int) = values().firstOrNull { it.position == position }

        /**
         * Count
         */
        fun count() = values().size
    }
}
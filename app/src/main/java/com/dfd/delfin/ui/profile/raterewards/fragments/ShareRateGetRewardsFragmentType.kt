package com.dfd.delfin.ui.profile.raterewards.fragments

import com.dfd.delfin.ui.base.BaseFragment
import com.dfd.delfin.ui.profile.raterewards.fragments.rewards.YourRewardsFragment
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateFragment

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
package com.dfd.delfin.ui.profile.raterewards.fragments

import com.dfd.delfin.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentActionType.Navigate

/**
 * Home fragment actions type
 */
enum class ShareRateGetRewardsFragmentActionType {
  Navigate
}

/**
 * Home fragment action base class
 */
abstract class BaseShareRateGetRewardsFragmentAction(val type: ShareRateGetRewardsFragmentActionType)

/**
 * Navigate to fragment [ShareRateGetRewardsFragmentType]
 */
class NavigateShareRateGetRewardsFragmentAction(val fragmentType: ShareRateGetRewardsFragmentType) : BaseShareRateGetRewardsFragmentAction(
  Navigate
)

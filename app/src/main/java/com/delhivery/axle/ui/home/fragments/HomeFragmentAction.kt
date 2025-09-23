package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.ui.home.fragments.HomeFragmentActionType.Navigate
import com.delhivery.axle.ui.home.fragments.HomeFragmentActionType.UpdatePlacementBadge

/**
 * Home fragment actions type
 */
enum class HomeFragmentActionType {
  Navigate,
  UpdatePlacementBadge
}

/**
 * Home fragment action base class
 */
abstract class BaseHomeFragmentAction(val type: HomeFragmentActionType)

/**
 * Navigate to fragment [HomeFragmentType]
 */
class NavigateHomeFragmentAction(val fragmentType: HomeFragmentType) : BaseHomeFragmentAction(
    Navigate
)

class NavigateHomeLoadsFragmentAction(val fragmentType: HomeLoadsTruckFragmentType) : BaseHomeFragmentAction(
    Navigate
)

/**
 * Update placement badge count
 */
class UpdatePlacementBadgeAction(val delayedCount: Int) : BaseHomeFragmentAction(
    UpdatePlacementBadge
)

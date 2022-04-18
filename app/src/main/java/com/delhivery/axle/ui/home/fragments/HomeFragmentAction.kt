package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.ui.home.fragments.HomeFragmentActionType.Navigate

/**
 * Home fragment actions type
 */
enum class HomeFragmentActionType {
  Navigate
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

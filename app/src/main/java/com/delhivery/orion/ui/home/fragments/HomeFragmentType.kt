package com.delhivery.orion.ui.home.fragments

import com.delhivery.orion.R
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.orion.ui.home.fragments.profile.HomeProfileFragment
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsFragment

/**
 * Home fragment type data
 */
enum class HomeFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: BaseFragment<*, *>,
  val title: String
) {
  LoadsFragment(R.id.nav_loads, 0, HomeLoadsFragment._instance, "Load Requests"),
  BidsFragment(R.id.nav_bids, 1, HomeBidsFragment._instance, "Bids & Requests"),
  TripsFragment(R.id.nav_trips, 2, HomeTripsFragment._instance, "Ongoing Trips"),
  ProfileFragment(R.id.nav_profile, 3, HomeProfileFragment._instance, "Profile");

  companion object {
    /**
     * Get fragment position by [menuId]
     */
    fun posById(menuId: Int) = values().filter { it.menuId == menuId }.firstOrNull()?.position ?: -1

    /**
     * Get [HomeFragmentType] by position
     */
    fun pos(position: Int) = values().filter { it.position == position }.firstOrNull()

    /**
     * Count
     */
    fun count() = values().size
  }
}
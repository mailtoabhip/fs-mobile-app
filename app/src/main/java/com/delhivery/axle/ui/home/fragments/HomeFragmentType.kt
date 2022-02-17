package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.R
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.pod.HomePodsFragment
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsFragment

/**
 * Home fragment type data
 */
enum class HomeFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: BaseFragment<*, *>,
  val title: String
) {
  LoadsFragment(R.id.nav_loads, 0, HomeLoadsFragment._instance, "Loads Requests"),
  BidsFragment(R.id.nav_bids, 1, HomeBidsFragment._instance, "Bids & Requests"),
  PodFragment(R.id.nav_pod, 2, HomePodsFragment._instance, "PODs"),
  TripsFragment(R.id.nav_trips, 3, HomeTripsFragment._instance, "Ongoing Trips");
  //Wallet(R.id.nav_wallet, 3, HomeWalletFragment._instance, "Balance"),
  //ProfileFragment(R.id.nav_profile, 4, MyProfileFragment._instance, "Profile");

  companion object {
    /**
     * Get fragment position by [menuId]
     */
    fun posById(menuId: Int) = values().firstOrNull { it.menuId == menuId }?.position ?: -1

    /**
     * Get [HomeFragmentType] by position
     */
    fun pos(position: Int) = values().firstOrNull { it.position == position }

    /**
     * Count
     */
    fun count() = values().size
  }
}
package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.R
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.home.fragments.pod.HomePodsFragment
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsFragment
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksFragment

/**
 * Home fragment type data
 */
enum class HomeFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: BaseFragment<*, *>,
  val title: String
) {
  LoadsTruckFragment(R.id.nav_loads_trucks, 0, HomeLoadsTruckFragment._instance, "Home"),
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

enum class HomeLoadsTruckFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: HomeLoadsTruckBaseFragment<*, *>,
  val title: String
){
  LoadsFragment(R.id.nav_loads,0,HomeLoadsFragment._instance, "Loads"),
  TruckFragment(R.id.nav_trucks,1, HomeTrucksFragment._instance,"My Trucks");

  companion object {
    /**
     * Get fragment position by [menuId]
     */
    fun posById(menuId: Int) = values().firstOrNull { it.menuId == menuId }?.position ?: -1

    /**
     * Get [HomeLoadsTruckFragmentType] by position
     */
    fun pos(position: Int) = values().firstOrNull { it.position == position }

    /**
     * Count
     */
    fun count() = values().size
  }
}
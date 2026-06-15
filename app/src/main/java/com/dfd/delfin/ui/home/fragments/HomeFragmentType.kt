package com.dfd.delfin.ui.home.fragments

import com.dfd.delfin.R
import com.dfd.delfin.ui.base.BaseFragment
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsFragment
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsFragment
import com.dfd.delfin.ui.home.fragments.home.HomeFragment
import com.dfd.delfin.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.dfd.delfin.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsDelayedFragment
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsExpectedFragment
import com.dfd.delfin.ui.home.fragments.pod.HomeNewPodFragment
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsFragment
import com.dfd.delfin.ui.home.fragments.trucks.HomeTrucksFragment

/**
 * Home fragment type data
 */
enum class HomeFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: BaseFragment<*, *>,
  val title: String
) {
  NewHomeFragment(R.id.nav_home, 0, HomeFragment._instance, "Home"),
  LoadsTruckFragment(R.id.nav_loads, 1, HomeLoadsTruckFragment._instance, "Loads"),
  TruckFragment(R.id.nav_trucks, 2, HomeTrucksFragment._instance, "My Trucks"),
  PodFragment(R.id.nav_pod, 3, HomeNewPodFragment._instance, "PODs"),
  TripsFragment(R.id.nav_trips, 4, HomeTripsFragment._instance, "Ongoing Trips");
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
  ContractsFragment(R.id.nav_trucks,1, HomeContractsFragment._instance,"Contracts"),
  BidsFragment(R.id.nav_trips, 2, HomeBidsFragment._instance, "Bids & Requests");
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

enum class HomePlacementsFragmentType(
  val menuId: Int,
  val position: Int,
  val fragment: HomeBaseFragment<*, *>,
  val title: String
){
  DelayedFragment(R.id.nav_delayed,0, HomePlacementsDelayedFragment._instance, "Delayed"),
  ExpectedFragment(R.id.nav_expected,1, HomePlacementsExpectedFragment._instance,"Expected");
  companion object {
    /**
     * Get fragment position by [menuId]
     */
    fun posById(menuId: Int) = values().firstOrNull { it.menuId == menuId }?.position ?: -1

    /**
     * Get [HomePlacementsFragmentType] by position
     */
    fun pos(position: Int) = values().firstOrNull { it.position == position }

    /**
     * Count
     */
    fun count() = values().size
  }
}
package com.delhivery.axle.ui.home.fragments.trips

import com.delhivery.axle.ui.custom.DelhiveryFabCardMenuItem

/**
 * Home trips fab card menu items
 */
val HomeTripsFabCardMenuItems by lazy {
  mutableListOf<DelhiveryFabCardMenuItem>().apply {
    add(0, DelhiveryFabCardMenuItem(0, "Ongoing trips"))
    add(1, DelhiveryFabCardMenuItem(1, "Completed trips"))
    add(2, DelhiveryFabCardMenuItem(2, "Pickup awaiting"))
    add(3, DelhiveryFabCardMenuItem(3, "Reached destination city"))
  }
}
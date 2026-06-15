package com.dfd.delfin.ui.home.fragments.trips

import com.dfd.delfin.ui.custom.DelfinFabCardMenuItem

/**
 * Home trips fab card menu items
 */
val HomeTripsFabCardMenuItems by lazy {
  mutableListOf<DelfinFabCardMenuItem>().apply {
    add(0, DelfinFabCardMenuItem(0, "Ongoing trips"))
    add(1, DelfinFabCardMenuItem(1, "Completed trips"))
    add(2, DelfinFabCardMenuItem(2, "Pickup awaiting"))
    add(3, DelfinFabCardMenuItem(3, "Reached destination city"))
  }
}
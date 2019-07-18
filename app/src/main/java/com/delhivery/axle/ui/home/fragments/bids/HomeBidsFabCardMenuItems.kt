package com.delhivery.axle.ui.home.fragments.bids

import com.delhivery.axle.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.axle.ui.custom.FabMenuCardMenuTitleItem

/**
 * Home bids fab card menu items
 */
val HomeBidsFabCardMenuItems by lazy {
  mutableListOf<DelhiveryFabCardMenuItem>().apply {
    add(0, DelhiveryFabCardMenuItem(FabMenuCardMenuTitleItem, "Sort by:"))
    add(1, DelhiveryFabCardMenuItem(0, "Date"))
    add(2, DelhiveryFabCardMenuItem(1, "Target price low to high"))
    add(3, DelhiveryFabCardMenuItem(2, "Truck Size"))
  }
}
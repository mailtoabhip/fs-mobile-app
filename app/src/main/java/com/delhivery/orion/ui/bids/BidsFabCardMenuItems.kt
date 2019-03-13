package com.delhivery.orion.ui.bids

import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem

/**
 * Bids fab card menu items
 */
val BidsFabCardMenuItems by lazy {
  mutableListOf<DelhiveryFabCardMenuItem>().apply {
    add(0, DelhiveryFabCardMenuItem(0, "Active Bids"))
    add(1, DelhiveryFabCardMenuItem(1, "Lost Bids"))
  }
}
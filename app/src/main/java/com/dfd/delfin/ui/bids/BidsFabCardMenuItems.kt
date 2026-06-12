package com.dfd.delfin.ui.bids

import com.dfd.delfin.ui.custom.DelhiveryFabCardMenuItem

/**
 * Bids fab card menu items
 */
val BidsFabCardMenuItems by lazy {
  mutableListOf<DelhiveryFabCardMenuItem>().apply {
    add(0, DelhiveryFabCardMenuItem(0, "Active Bids"))
    add(1, DelhiveryFabCardMenuItem(1, "Lost Bids"))
  }
}
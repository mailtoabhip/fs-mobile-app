package com.dfd.delfin.ui.bids

import com.dfd.delfin.ui.custom.DelfinFabCardMenuItem

/**
 * Bids fab card menu items
 */
val BidsFabCardMenuItems by lazy {
  mutableListOf<DelfinFabCardMenuItem>().apply {
    add(0, DelfinFabCardMenuItem(0, "Active Bids"))
    add(1, DelfinFabCardMenuItem(1, "Lost Bids"))
  }
}
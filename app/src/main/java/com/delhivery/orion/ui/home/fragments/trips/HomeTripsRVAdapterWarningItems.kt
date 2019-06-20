package com.delhivery.orion.ui.home.fragments.trips

import com.delhivery.orion.data.home.trips.HomeTripsWarningAction_NoLoads
import com.delhivery.orion.data.home.trips.HomeTripsWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomeTripsWarningItem_NoLoads = HomeTripsWarningItem(
    HomeTripsWarningItemData(
        "No Trips found",
        "Bid smartly and win to get your trucks on the trips…",
        "Start Bidding", HomeTripsWarningAction_NoLoads
    )
)


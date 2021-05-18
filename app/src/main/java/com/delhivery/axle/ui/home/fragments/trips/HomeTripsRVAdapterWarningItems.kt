package com.delhivery.axle.ui.home.fragments.trips

import com.delhivery.axle.data.home.trips.HomeTripsTimeOutAction
import com.delhivery.axle.data.home.trips.HomeTripsWarningAction_NoTrips
import com.delhivery.axle.data.home.trips.HomeTripsWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomeTripsWarningItem_NoTrips = HomeTripsWarningItem(
    HomeTripsWarningItemData(
        "No Trips found",
        "Start bidding now !",
        "View Trips Summary", HomeTripsWarningAction_NoTrips
    )
)

val HomeTripsWarningItem_TimeOut = HomeTripsWarningItem(
    HomeTripsWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomeTripsTimeOutAction
    )
)


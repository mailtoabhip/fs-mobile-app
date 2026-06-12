package com.dfd.delfin.ui.home.fragments.pod

import com.dfd.delfin.data.home.pod.HomePodWarningAction_NoTrips
import com.dfd.delfin.data.home.pod.HomePodWarningAction_TimeOut
import com.dfd.delfin.data.home.pod.HomePodWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomePodWarningItem_NoLoads = HomePodWarningItem(
    HomePodWarningItemData(
        "No Trips found",
        "Start bidding now !",
        "View Available Load", HomePodWarningAction_NoTrips
    )
)

val HomePodWarningItem_TimeOut = HomePodWarningItem(
    HomePodWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomePodWarningAction_TimeOut
    )
)


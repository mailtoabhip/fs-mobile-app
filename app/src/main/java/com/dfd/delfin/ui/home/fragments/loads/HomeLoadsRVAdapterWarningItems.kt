package com.dfd.delfin.ui.home.fragments.loads

import com.dfd.delfin.data.home.loads.HomeLoadsTimeOutAction
import com.dfd.delfin.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.dfd.delfin.data.home.loads.HomeLoadsWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomeLoadsWarningItem_NoLoads = HomeLoadsWarningItem(
    HomeLoadsWarningItemData(
        "No Loads found",
        "Please select your route preference to see the load requests",
        "Select Routes", HomeLoadsWarningAction_NoLoads
    )
)

val HomeLoadsWarningItem_TimeOut = HomeLoadsWarningItem(
    HomeLoadsWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomeLoadsTimeOutAction
    )
)
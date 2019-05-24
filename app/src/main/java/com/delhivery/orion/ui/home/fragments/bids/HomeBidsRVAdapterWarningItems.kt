package com.delhivery.orion.ui.home.fragments.bids

import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_EditRoutePrefs
import com.delhivery.orion.data.home.bids.HomeBidsWarningAction_SelectRoutes
import com.delhivery.orion.data.home.bids.HomeBidsWarningItemData

/**
 * Select routes warning item, when no routes are found with user lane_prefs
 *
 * @Zeplin https://zpl.io/VxLQ4Nx
 */
val HomeBidsWarningItem_SelectRoutes = HomeBidsWarningItem(
    HomeBidsWarningItemData(
        "No Routes selected",
        "Please select your route preference to see the load requests",
        "Select routes", HomeBidsWarningAction_SelectRoutes
    )
)

/**
 * Edit route prefs warning item, when no transactions are found
 *
 * @Zeplin https://zpl.io/2yOGeXw
 */
val HomeBidsWarningItem_EditRoutePrefs = HomeBidsWarningItem(
    HomeBidsWarningItemData(
        "No Routes selected",
        "Please select your route preference to see the load requests",
        "Select routes", HomeBidsWarningAction_EditRoutePrefs
    )
)


package com.delhivery.orion.ui.home.fragments.loads

import com.delhivery.orion.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.orion.data.home.loads.HomeLoadsWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomeLoadsWarningItem_NoLoads = HomeLoadsWarningItem(
    HomeLoadsWarningItemData(
        "No Loads found",
        "Please add more route preference to see the load requests",
        "Add Routes", HomeLoadsWarningAction_NoLoads
    )
)


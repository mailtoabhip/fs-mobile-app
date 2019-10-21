package com.delhivery.axle.ui.home.fragments.bids

import com.delhivery.axle.data.home.bids.HomeBidsTimeOutAction
import com.delhivery.axle.data.home.bids.HomeBidsWarningAction_NoBids
import com.delhivery.axle.data.home.bids.HomeBidsWarningItemData
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchLoadsWarningItem

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val HomeBidsWarningItem_NoBids = HomeBidsWarningItem(
    HomeBidsWarningItemData(
        "No Bids found",
        "Your’s has to be the lowest bid to get it confirmed. Bid more..",
        "START BIDDING", HomeBidsWarningAction_NoBids
    )
)

val HomeBidsWarningItem_TimeOut = HomeBidsWarningItem(
    HomeBidsWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomeBidsTimeOutAction
    )
)

val SearchLoadWarningItem_NoLoad = SearchLoadsWarningItem(
    HomeBidsWarningItemData(
        "No Loads found",
        "Please change the search parameters",
        "", HomeBidsWarningAction_NoBids
    )
)


package com.dfd.delfin.ui.home.fragments.placements

import com.dfd.delfin.data.home.placements.HomePlacementsTimeoutItemAction
import com.dfd.delfin.data.home.placements.HomePlacementsTimeoutItemData
import com.dfd.delfin.data.home.placements.HomePlacementsWarningAction_NoLoads
import com.dfd.delfin.data.home.placements.HomePlacementsWarningItemData

val HomePlacementsWarningItem_NoLoads = HomePlacementsWarningItem(
    HomePlacementsWarningItemData(
        "No Placement found",
        " ",
        "REFRESH", HomePlacementsWarningAction_NoLoads
    )
)

val HomePlacementsWarningItem_TimeOut = HomePlacementsTimeoutItem(
    HomePlacementsTimeoutItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomePlacementsTimeoutItemAction
    )
)

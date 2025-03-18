package com.delhivery.axle.ui.home.fragments.placements

import com.delhivery.axle.data.home.contracts.HomeContractsTimeOutAction
import com.delhivery.axle.data.home.contracts.HomeContractsWarningItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemAction
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemData
import com.delhivery.axle.data.home.placements.HomePlacementsWarningAction_NoLoads
import com.delhivery.axle.data.home.placements.HomePlacementsWarningItemData
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsWarningItem

val HomePlacementsWarningItem_NoLoads = HomePlacementsWarningItem(
    HomePlacementsWarningItemData(
        "No Contracts found",
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
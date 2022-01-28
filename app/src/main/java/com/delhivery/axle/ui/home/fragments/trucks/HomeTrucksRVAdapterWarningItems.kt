package com.delhivery.axle.ui.home.fragments.trucks

import com.delhivery.axle.data.home.trucks.HomeTrucksTimeOutAction
import com.delhivery.axle.data.home.trucks.HomeTrucksWarningAction_NoTrucks
import com.delhivery.axle.data.home.trucks.HomeTrucksWarningItemData

val HomeTrucksWarningItem_NoTrucks = HomeTrucksWarningItem(
    HomeTrucksWarningItemData(
        "No Trucks found",
        "Please add trucks",
        "Add Truck", HomeTrucksWarningAction_NoTrucks
    )
)

val HomeLoadsWarningItem_TimeOut = HomeTrucksWarningItem(
    HomeTrucksWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomeTrucksTimeOutAction
    )
)
package com.dfd.delfin.ui.home.fragments.trucks

import com.dfd.delfin.data.home.trucks.HomeTrucksTimeOutAction
import com.dfd.delfin.data.home.trucks.HomeTrucksWarningAction_NoTrucks
import com.dfd.delfin.data.home.trucks.HomeTrucksWarningItemData

val HomeTrucksWarningItem_NoTrucks = HomeTrucksWarningItem(
    HomeTrucksWarningItemData(
        "No Trucks found",
        "Please add trucks",
        "Add Truck", HomeTrucksWarningAction_NoTrucks
    )
)

val HomeTrucksWarningItem_TimeOut = HomeTrucksWarningItem(
    HomeTrucksWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", HomeTrucksTimeOutAction
    )
)
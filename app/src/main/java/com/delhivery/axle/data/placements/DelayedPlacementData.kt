package com.delhivery.axle.data.placements

import com.delhivery.axle.R

data class DelayedPlacementData(
    val delayText: String = "Delayed by 2 hrs",
    val routeText: String = "Kochi, Kerala → Jaipur, Rajasthan",
    val stopsText: String = "3 stops",
    val clientBadge: ClientBadgeData = ClientBadgeData(),
    val truckInfo: InfoItemData = InfoItemData(
        iconRes = 0,
        text = "Open 32FTXXL18 MT",
        iconContentDescription = "Truck icon",
        textContentDescription = "Truck type and capacity"
    ),
    val timeInfo: InfoItemData = InfoItemData(
        iconRes = 0,
        text = "Report Today, 11:00 am",
        iconContentDescription = "Clock icon",
        textContentDescription = "Reporting time"
    ),
    val vehicleInfo: InfoItemData = InfoItemData(
        iconRes = 0,
        text = "DL79FL7982",
        iconContentDescription = "ID card icon",
        textContentDescription = "Vehicle number"
    ),
    val driverInfo: InfoItemData = InfoItemData(
        iconRes = 0,
        text = "Mohan Aggarwal",
        iconContentDescription = "User icon",
        textContentDescription = "Driver name"
    ),
    val phoneInfo: InfoItemData = InfoItemData(
        iconRes = 0,
        text = "9876259876",
        iconContentDescription = "Phone icon",
        textContentDescription = "Driver phone number"
    )
)

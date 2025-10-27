package com.delhivery.axle.utils

import android.view.View
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.placements.DelayedPlacementData
import com.delhivery.axle.data.placements.ClientBadgeData
import com.delhivery.axle.data.placements.InfoItemData
import com.delhivery.axle.R

object PlacementDataMapper {
    
    fun mapToDelayedPlacementData(data: HomePlacementsItemData): DelayedPlacementData {
        return DelayedPlacementData(
            delayText = "Delayed by ${data.formatReportingTime() ?: "unknown time"}",
            routeText = "${data.originCenterName}, ${data.originCenterState} → ${data.destinationCenterName}, ${data.destinationCenterState}",
            stopsText = data.haltStops(),
            clientBadge = ClientBadgeData(
                text = data.loadType() ?: "Contract",
                iconRes = R.drawable.ic_bolt,
                showCloseButton = true
            ),
            truckInfo = InfoItemData(
                iconRes = R.drawable.ic_truck,
                text = "${data.vehicleType} ${data.vehicleNumber ?: ""}",
                iconContentDescription = "Truck icon",
                textContentDescription = "Truck type and number"
            ),
            timeInfo = InfoItemData(
                iconRes = R.drawable.ic_clock,
                text = "Report ${data.formatReportingTime() ?: "unknown time"}",
                iconContentDescription = "Clock icon",
                textContentDescription = "Reporting time"
            ),
            vehicleInfo = InfoItemData(
                iconRes = R.drawable.ic_id_card,
                text = data.vehicleNumber ?: "Not available",
                iconContentDescription = "ID card icon",
                textContentDescription = "Vehicle number"
            ),
            driverInfo = InfoItemData(
                iconRes = R.drawable.ic_user,
                text = data.driverName() ?: "Not available",
                iconContentDescription = "User icon",
                textContentDescription = "Driver name"
            ),
            phoneInfo = InfoItemData(
                iconRes = R.drawable.ic_phone,
                text = data.driverPhone ?: "Not available",
                iconContentDescription = "Phone icon",
                textContentDescription = "Driver phone number"
            )
        )
    }
    
    /**
     * Get visibility for vehicle number based on available data
     */
    fun getVehicleNumberVisibility(data: HomePlacementsItemData): Int {
        return if (data.vehicleNumber.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    
    /**
     * Get visibility for driver name based on available data
     */
    fun getDriverNameVisibility(data: HomePlacementsItemData): Int {
        return if (data.driverName.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    
    /**
     * Get visibility for driver phone based on available data
     */
    fun getDriverPhoneVisibility(data: HomePlacementsItemData): Int {
        return if (data.driverPhone.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    
    /**
     * Get visibility for driver details section (name or phone)
     */
    fun getDriverDetailsVisibility(data: HomePlacementsItemData): Int {
        return if (data.driverName.isNullOrBlank() && data.driverPhone.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    
    /**
     * Get visibility for vehicle details section
     */
    fun getVehicleDetailsVisibility(data: HomePlacementsItemData): Int {
        return if (data.vehicleNumber.isNullOrBlank() && data.vehicleType.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    
    /**
     * Get text for vehicle number with fallback
     */
    fun getVehicleNumberText(data: HomePlacementsItemData): String {
        return data.vehicleNumber?.takeIf { it.isNotBlank() } ?: "Vehicle details not available"
    }
    
    /**
     * Get text for driver name with fallback
     */
    fun getDriverNameText(data: HomePlacementsItemData): String {
        return data.driverName()?.takeIf { it.isNotBlank() } ?: "Driver name not available"
    }
    
    /**
     * Get text for driver phone with fallback
     */
    fun getDriverPhoneText(data: HomePlacementsItemData): String {
        return data.driverPhone?.takeIf { it.isNotBlank() } ?: "Phone not available"
    }
}

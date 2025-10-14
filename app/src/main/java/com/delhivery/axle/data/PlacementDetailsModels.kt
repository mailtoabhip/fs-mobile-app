package com.delhivery.axle.data

import com.google.gson.annotations.SerializedName

data class VehicleModel(
    @SerializedName("truck_display_name") val truckDisplayName: String?,
    @SerializedName("truck_type") val truckType: String?,
)


data class RouteInformation(
    @SerializedName("origin") val origin: Origin?,
    @SerializedName("destination") val destination: Destination?,
    @SerializedName("halt_centers") val haltCenters: List<HaltCenters>?,
    @SerializedName("route_days_of_week") val routeDaysOfWeek: List<Int>?,
)

data class Origin(
    @SerializedName("center_code") val centerCode: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_state") val centerState: String?,
    @SerializedName("coordinates") val coordinates: Coordinates?,
    @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
    @SerializedName("rel_eta") val relETA: String?,
    @SerializedName("rel_etd") val relETD: String?,
)

data class Destination(
    @SerializedName("center_code") val centerCode: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_state") val centerState: String?,
    @SerializedName("coordinates") val coordinates: Coordinates?,
    @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
    @SerializedName("rel_eta") val relETA: String?,
    @SerializedName("rel_etd") val relETD: String?,
)

data class Coordinates(
    @SerializedName("lat") val lat: Int?,
    @SerializedName("lon") val lon: Int?,
)

data class CostDataObject(
    @SerializedName("kpm") val kpm: Int?,
    @SerializedName("hpd") val hpd: Int?,
    @SerializedName("dpm") val dpm: Int?,
    @SerializedName("rpd") val rpd: Int?,
    @SerializedName("rpk") val rpk: Int?,
    @SerializedName("rph") val rph: Int?,
    @SerializedName("nep") val nep: Int?,
)


data class HaltCenters(
    @SerializedName("center_code") val centerCode: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_state") val centerState: String?,
    @SerializedName("coordinates") val coordinates: Coordinates?,
    //
    @SerializedName("halt_hrs") val haltHours: Int?,
    @SerializedName("position") val position: Int?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    //
    @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
    @SerializedName("rel_eta") val relETA: String?,
    @SerializedName("rel_etd") val relETD: String?
)

package com.delhivery.axle.data

import com.google.gson.annotations.SerializedName

data class VehicleModel(
    @SerializedName("truck_display_name") val truckDisplayName: String?,
    @SerializedName("truck_type") val truckType: String?
)


data class RouteInformation(
    @SerializedName("origin") val origin: Center?,
    @SerializedName("destination") val destination: Center?,
    @SerializedName("halt_centers") val haltCenters: List<HaltCenters>?,
    @SerializedName("route_days_of_week") val routeDaysOfWeek: List<Int>?
)

data class Center(
    @SerializedName("center_code") val centerCode: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_state") val centerState: String?,
    @SerializedName("coordinates") val coordinates: Coordinates?,
    @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
    @SerializedName("rel_eta") val relETA: String?,
    @SerializedName("rel_etd") val relETD: String?
)

//data class Destination(
//    @SerializedName("center_code") val centerCode: String?,
//    @SerializedName("center_name") val centerName: String?,
//    @SerializedName("center_state") val centerState: String?,
//    @SerializedName("coordinates") val coordinates: Coordinates?,
//    @SerializedName("past_travel_hrs") val pastTravelHrs: Int?,
//    @SerializedName("rel_eta") val relETA: String?,
//    @SerializedName("rel_etd") val relETD: String?,
//)

data class Coordinates(
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?
)

data class CostDataObject(
    @SerializedName("kpm") val kpm: Double?,
    @SerializedName("hpd") val hpd: Double?,
    @SerializedName("dpm") val dpm: Double?,
    @SerializedName("rpd") val rpd: Double?,
    @SerializedName("rpk") val rpk: Double?,
    @SerializedName("rph") val rph: Double?,
    @SerializedName("nep") val nep: Boolean
)

data class Misc(
    @SerializedName("client_name") val clientName: String?,
    @SerializedName("material_type") val materialType: String?
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

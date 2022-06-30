package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Payload for update price
 */
data class UpdatePriceRequest(
        @SerializedName("source") var source: String?,
        @SerializedName("origin_city_code") var originCityCode: String?,
        @SerializedName("origin_city") var originCity: String?,
        @SerializedName("origin_cluster_id") var originClusterId: String?,
        @SerializedName("destination_city_code") var destinationCityCode: String?,
        @SerializedName("destination_city") var destinationCity: String?,
        @SerializedName("destination_cluster_id") var destinationClusterId: String?,
        @SerializedName("truck_display_name") var truckDisplayName: String?,
        @SerializedName("truck_capacity") var truckCapacity: Double?,
        @SerializedName("vehicle_number") var vehicleNumber: String?,
        @SerializedName("rate") var rate: String?,
        @SerializedName("trip_date") var tripDate: String?,
        @SerializedName("sp_id") var spId: String?,
        @SerializedName("sp_name") var spName: String?,
        @SerializedName("child_sp_id") var childSpId: String?,
        @SerializedName("rate_type") var rateType: String?,
        @SerializedName("proof_type") var proofType: String?,
        @SerializedName("proof_url") var proofUrl: List<String>?,
        @SerializedName("phone_number") var phoneNumber: String?
)

data class OfferRequest(
        @SerializedName("all_offers") var allOffers:Boolean = true,
        @SerializedName("source") var source:List<String> = arrayListOf("origin_city_code", "destination_city_code", "truck_display_name","start_date","end_date", "amount", "origin_city", "destination_city")
)

data class keyLists(
        @SerializedName("key") var key:ArrayList<String> = arrayListOf<String>()
)

data class rangeFiltersData(
        @SerializedName("column") var column:String = "key",
        @SerializedName("operator") var operator:String = "gte/lte",
        @SerializedName("value") var value:String = "YYYY:MM:ddT00:00:00"
)
data class OfferObjectResponse(
  @SerializedName("offers") var offersList:List<OfferResponse>
)
data class OfferResponse(
        @SerializedName("origin_city") var oc: String?,
        @SerializedName("origin_city_code") var occ: String?,
        @SerializedName("destination_city") var dc: String?,
        @SerializedName("destination_city_code") var dcc: String?,
        @SerializedName("truck_display_name") var tdn: String?,
        @SerializedName("status") var status: String?,
        @SerializedName("start_date") var sd: String?,
        @SerializedName("end_date") var ed: String?,
        @SerializedName("amount") var amount:Double?,
        @SerializedName("offer_type") var offer_type: String?
)

data class OfferState(
        @SerializedName("updated") var updated:Boolean?
)

data class TruckSpecifications(
        @SerializedName("truck_uuid") val truck_uuid: String,
        @SerializedName("capacity") val capacity: Double,
        @SerializedName("truck_type") val truckType: String
): Serializable
package com.delhivery.axle.api.request

import com.delhivery.axle.data.RouteMappingModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * Payload for updating user base city
 */
data class UpdateUserBaseCityRequest(
  @SerializedName("base_city") val city: String,
  @SerializedName("base_city_code") val cityCode: String,
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
)

/**
 * Payload for updating user routes
 */
data class UpdateUserRoutesRequest(
  @SerializedName("lane_preferences") val routes: List<RouteMappingModel>
) {

  fun getRequest(): JsonObject {
    val jsonObject = JsonObject()
    val jsonArray = JsonArray()
    routes.forEach {
      val json = JsonObject()
      val originjson = JsonObject()
      it.origin.city.let { it1 -> originjson.addProperty("city", it1) }
      it.origin.orion_db_city_code?.let { it1 -> originjson.addProperty("city_id", it1) }
      json.add("origin", originjson)

      val destinationjson = JsonObject()
      it.destination.state.let { it1 -> destinationjson.addProperty("state", it1) }
      it.destination.stateId?.let { it1 -> destinationjson.addProperty("state_id", it1) }
      json.add("destination", destinationjson)
      jsonArray.add(json)
    }

    jsonObject.add("lane_preferences", jsonArray)
    return jsonObject
  }
}

/**
 * Payload for update user access
 */
data class UpdateUserAccessRequest(
  @SerializedName("accessed_by_axle") val accessed: Boolean = true
)

/**
 * Payload for FCM registration
 */
data class UpdateUserFCMTokenRequest(
  @SerializedName("fcm_reg_token") val token: String
) {
  companion object {
    /**
     * @param fcmToken FCM token
     */
    fun getRequest(fcmToken: String) = UpdateUserFCMTokenRequest(fcmToken)
  }
}

/* actions */
const val EditTeamMemberAction_Edit = "edit_member"
const val DeleteTeamMemberAction_Delete = "delete_member"
const val ViewAdminMember ="view_admin"
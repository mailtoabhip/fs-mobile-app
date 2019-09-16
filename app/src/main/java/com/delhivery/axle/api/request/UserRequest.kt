package com.delhivery.axle.api.request

import com.delhivery.axle.data.RouteMappingModel
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
)

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
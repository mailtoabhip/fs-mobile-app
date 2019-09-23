package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class NotificationReadRequest(
  @SerializedName("notification_ids") val ids: MutableList<String>,
  @SerializedName("source") val source: String = "axle-app"
) {
  companion object {
    /**
     * Get appended phone no request
     *
     * @param id notification id
     *
     */
    fun getRequest(id: String) = NotificationReadRequest(mutableListOf(id))
  }
}
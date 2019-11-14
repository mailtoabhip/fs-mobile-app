package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for upload POD
 */
data class PodRequest(
  @SerializedName("trip_id") val transactionId: String,
  @SerializedName("pod_files") val paths: List<String>
) {
  companion object {
    /**
     *
     *
     * @return [PodRequest] request body
     */
    fun getRequest(
      transactionId: String,
      paths: List<String>
    ) = PodRequest(transactionId, paths)
  }
}
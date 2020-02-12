package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for upload POD
 */
data class PodRequest(
  @SerializedName("pod_image_urls") val paths: List<String>,
  @SerializedName("action") val action: String = "pod_uploaded",
  @SerializedName("originator") val originator: String = "axle-app"
) {
  companion object {

    /**
     * @return [PodRequest] request body
     */
    fun getRequest(paths: List<String>) = PodRequest(paths)
  }
}

/**
 * Payload to update dispatch detail
 */
data class UpdateDispatchRequest(
  @SerializedName("data") val data: List<DispatchData>
)

/**
 * Dispatch data
 */
data class DispatchData(
  @SerializedName("trip_id") val transactionId: String,
  @SerializedName("pod_dispatch_awb_number") val trakingNumber: String,
  @SerializedName("pod_dispatch_docket_image") val imagePAth: String,
  @SerializedName("pod_dispatch_date") val date: String
)
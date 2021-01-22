package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

/**
 * Payload for upload POD
 */
data class PodRequest(
  @SerializedName("action_code") var action_code: String?,
  @SerializedName("action_sub_code") var action_sub_code: String?,
  @SerializedName("trip_id") var trip_id: String?,
  @SerializedName("data") var data: PODData?
)

data class PODData(
  @SerializedName("pod_image_urls") var podImages: List<String>,
  @SerializedName("originator") var originator: String,
  @SerializedName("reached_time") var reachedTime: String,
  @SerializedName("unloaded_time") var unloadedTime: String,
  @SerializedName("physical_pod_received") var physicalPODreceived: String,
  @SerializedName("epod_uploaded_by_user_id") var userId: String,
  @SerializedName("epod_uploaded_by_name") var userName: String,
  @SerializedName("epod_uploaded_by_phone") var userPhone: String
)

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
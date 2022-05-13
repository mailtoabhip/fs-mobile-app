package com.delhivery.axle.api.response

import com.delhivery.axle.data.UserModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 4/1/21
 */

/**
 * Response container for user team detail api
 */
data class UserDetailResponse(
  @SerializedName("count") val count: Int,
  @SerializedName("total") val total: Int,
  @SerializedName("items") val users: List<UserModel>
)

data class CreateUserResponse(
  @SerializedName("message") val message: String,
  @SerializedName("uuid") val uuid: String
)

data class KYCDetailResponse(
        @SerializedName("count") val count: Int,
        @SerializedName("data") val kycData: List<KYCResponse>?
)

data class KYCResponse(
        @SerializedName("document_urls") val documentUrls: List<String>?,
        @SerializedName("verification_status") val verificationStatus: String?,
        @SerializedName("verification_overall_type") val verificationOverallType: String?,
        @SerializedName("verification_type") val verificationType: String?,
        @SerializedName("verification_status_reason_code") val verificationStatusReasonCode: String?,
        @SerializedName("verification_status_reason_message") val verificationStatusReasonMessage: String?
)

data class TeamDetailResponse(
        @SerializedName("count") val count: Int,
        @SerializedName("data") val users: List<UserModel>
)
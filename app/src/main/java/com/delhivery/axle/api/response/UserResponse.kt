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
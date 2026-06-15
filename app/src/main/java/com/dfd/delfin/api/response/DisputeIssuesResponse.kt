package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class DisputeIssuesResponse(
    @SerializedName("disputeTypes")
    val disputeTypes: List<DisputeType>?
)

data class DisputeType(
    @SerializedName("code")
    val code: String?,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("sortOrder")
    val sortOrder: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("addTxnReq")
    val addTxnReq: Boolean?,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("sub-title")
    val subTitle: String? = null
)

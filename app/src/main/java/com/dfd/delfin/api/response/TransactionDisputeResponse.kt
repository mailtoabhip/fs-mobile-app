package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class TransactionDisputeResponse(
    @SerializedName("txn_id")
    val txnId: String?,
    @SerializedName("fastag_id")
    val fastagId: String?,
    @SerializedName("fastag_issued_by")
    val fastagIssuedBy: String?,
    @SerializedName("txn_category")
    val txnCategory: String?,
    @SerializedName("txn_datetime")
    val txnDatetime: String?,
    @SerializedName("txn_type")
    val txnType: String?,
    @SerializedName("txn_amount")
    val txnAmount: Double?,
    @SerializedName("toll_plaza_name")
    val tollPlazaName: String?,
    @SerializedName("toll_plaza_id")
    val tollPlazaId: String?,
    @SerializedName("dispute_details")
    val disputeDetails: DisputeDetails?
)

data class DisputeDetails(
    @SerializedName("issue_category")
    val issueCategory: String?,
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("sr_id")
    val srId: String?,
    @SerializedName("current_status")
    val currentStatus: String?,
    @SerializedName("current_status_color")
    val currentStatusColor: String?,
    @SerializedName("status_timeline")
    val statusTimeline: List<StatusTimeline>?
)

data class StatusTimeline(
    @SerializedName("status")
    val status: String?,
    @SerializedName("changedAt")
    val changedAt: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("statusChangeComments")
    val statusChangeComments: String?,
    @SerializedName("statusChangedBy")
    val statusChangedBy: String?
)

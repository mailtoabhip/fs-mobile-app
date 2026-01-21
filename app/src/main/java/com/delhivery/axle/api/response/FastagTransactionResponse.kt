package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagTransactionResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("data")
    val data: FastagTransactionData?
)

data class FastagTransactionData(
    @SerializedName("has_next")
    val hasNext: Boolean?,

    @SerializedName("count")
    val count: Int?,
    
    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("transactions")
    val transactions: List<FastagTransaction>?
)

data class FastagTransaction(

    @SerializedName("fastag_id")
    val tagId: String?,

    @SerializedName("txn_id")
    val txnId: String?,

    @SerializedName("txn_amount")
    val amount: Double?,

    @SerializedName("txn_type")
    val transactionType: String?,

    @SerializedName("fastag_avl_balance")
    val avlBalance: String?,

    @SerializedName("txn_time")
    val timestamp: String?,

    @SerializedName("txn_event")
    val tollName: String?,

    @SerializedName("txn_details")
    val txnDetails: String?
)



package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for GET /fastag/transactions API
 * Used for fetching transactions by toll plaza
 */
data class FastagTransactionsByTollPlazaResponse(
    @SerializedName("total_count")
    val totalCount: String?,

    @SerializedName("next_offset")
    val nextOffset: String?,

    @SerializedName("transactions")
    val transactions: List<FastagTransactionByTollPlaza>?
)

data class FastagTransactionByTollPlaza(
    @SerializedName("txn_id")
    val txnId: String?,

    @SerializedName("toll_plaza_name")
    val tollPlazaName: String?,

    @SerializedName("txn_amount")
    val txnAmount: Double?,

    @SerializedName("txn_type")
    val txnType: String?,

    @SerializedName("toll_plaza_id")
    val tollPlazaId: String?,

    @SerializedName("txn_dateTime")
    val txnDateTime: String?
)

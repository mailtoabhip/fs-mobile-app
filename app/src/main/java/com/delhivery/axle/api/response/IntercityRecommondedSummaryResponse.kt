package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName


data class LoadCounts(
    @SerializedName("all") val all: List<LoadsCountItem>,
)


data class LoadsCountItem(
    @SerializedName("key") val key: String?,
    @SerializedName("doc_count") val count: Int?=0
)

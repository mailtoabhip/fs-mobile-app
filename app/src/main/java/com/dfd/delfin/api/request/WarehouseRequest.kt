package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class WarehouseRequest (
    @SerializedName("warehouse_type") var warehouseType: String,
    @SerializedName("warehouse_id") var warehouseId: String,
    @SerializedName("source") var source: String
)

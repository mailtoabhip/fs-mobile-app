package com.delhivery.axle.api.response

import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName

data class WarehouseDetailResponse(
  @SerializedName("count") val count: Int,
  @SerializedName("items") val warehouses: List<Warehouse>
)

data class Warehouse(
  @SerializedName("warehouse_name") val name: String,
  @SerializedName("address") val address: String
) {

  fun completeAddress() = "${StringUtils.capitalize(name)}, ${StringUtils.capitalize(address)}"

}
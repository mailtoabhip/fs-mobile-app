package com.delhivery.axle.api.request

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class SearchRequest() : BaseKeyTypeModel<String>() {

  override fun key() = "${tripStatus ?: ""}${vendorId ?: ""}${vehicleNumber ?: ""}${lr ?: ""}"

  var tripStatus: String? = null
  var vendorId: String? = null
  var offset: Int? = null
  var vehicleNumber: String? = null
  var lr: String? = null
  var updatedAfter: String? = null
  var limit: Int? = null
  var result: Int? = null
  var column: String? = "unloaded"
  var operator: String? = "gte"
  var value: String? = null

  constructor(
    vehicleNumber: String? = null,
    lr: String? = null,
    result: Int? = null,
    tripStatus: String? = null,
    offset: Int? = null,
    limit: Int? = null
  ) : this() {
    this.vehicleNumber = vehicleNumber
    this.lr = lr
    this.result = result
    this.tripStatus = tripStatus
    this.offset = offset
    this.limit = limit
  }

  fun getRequest(): JsonObject {
    val jsonObject = JsonObject()
    tripStatus?.let { if (it.isNotEmpty()) jsonObject.addProperty("status_list", it) }
    vehicleNumber?.let {
      if (it.isNotEmpty()) jsonObject.addProperty(
          "vehicle_number", it.toUpperCase()
      )
    }
    lr?.let { if (it.isNotEmpty()) jsonObject.addProperty("LR", it) }
    vendorId?.let { if (it.isNotEmpty()) jsonObject.addProperty("vendor_id", it) }
    updatedAfter?.let { if (it.isNotEmpty()) jsonObject.addProperty("updated_after", it) }
    offset?.let { jsonObject.addProperty("offset", it) }
    limit?.let { jsonObject.addProperty("limit", it) }
    value?.let {
      val jsonArray = JsonArray()
      val rangeJsonObject = JsonObject()
      rangeJsonObject.addProperty("column", column)
      rangeJsonObject.addProperty("operator", operator)
      rangeJsonObject.addProperty("value", value)
      jsonArray.add(rangeJsonObject)
      jsonObject.add("range_filters", jsonArray)
    }
    return jsonObject
  }

  fun getResultString(): String {
    var resultString = "$result trips with"
    vehicleNumber?.let { resultString = "$resultString Vehicle Number: $it," }
    lr?.let { resultString = "$resultString LR Number: $it," }
    return resultString.substring(0, resultString.length - 1)
  }

}

/* actions */
const val SearchAction_SearchTrip = "search_trip"
const val SearchAction_ResetTrip = "reset_trip"
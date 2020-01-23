package com.delhivery.axle.api.request

import com.delhivery.axle.data.BaseKeyTypeModel
import com.google.gson.JsonObject

class SearchRequest : BaseKeyTypeModel<String>() {

  override fun key() = "${tripStatus ?: ""}${vendorId ?: ""}"

  var tripStatus: String? = null
  var vendorId: String? = null
  var offset: Int? = null
  var limit: Int? = null

  fun getRequest(): JsonObject {
    val jsonObject = JsonObject()
    tripStatus?.let { if (it.isNotEmpty()) jsonObject.addProperty("status_list", it) }
    vendorId?.let { if (it.isNotEmpty()) jsonObject.addProperty("vendor_id", it) }
    offset?.let { jsonObject.addProperty("offset", it) }
    limit?.let { jsonObject.addProperty("limit", it) }
    return jsonObject
  }
}
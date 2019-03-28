package com.delhivery.orion.data

import com.google.gson.annotations.SerializedName

data class StateModel(
  @SerializedName("state") val state: String,
  @SerializedName("state_id") val stateId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = stateId
}

val StateModelList = listOf<StateModel>(
    StateModel("andaman & nicobar islands", "AN"),
    StateModel("andhra pradesh", "AP"),
    StateModel("arunachal pradesh", "AR"),
    StateModel("assam", "AS"),
    StateModel("bihar", "BR"),
    StateModel("chandigarh", "CH"),
    StateModel("chhattisgarh", "CG"),
    StateModel("dadra & nagar haveli", "DH"),
    StateModel("daman & diu", "DD"),
    StateModel("delhi", "DL"),
    StateModel("goa", "GA"),
    StateModel("gujarat", "GJ"),
    StateModel("haryana", "HR"),
    StateModel("himachal pradesh", "HP"),
    StateModel("jammu & kashmir", "JK"),
    StateModel("jharkhand", "JH"),
    StateModel("karnataka", "KA"),
    StateModel("kerala", "KL"),
    StateModel("madhya pradesh", "MP"),
    StateModel("maharashtra", "MH"),
    StateModel("meghalaya", "ML"),
    StateModel("mizoram", "MZ"),
    StateModel("nagaland", "NL"),
    StateModel("odisha", "OR"),
    StateModel("puducherry", "PY"),
    StateModel("punjab", "PB"),
    StateModel("rajasthan", "RJ"),
    StateModel("sikkim", "SK"),
    StateModel("state	state", "id"),
    StateModel("tamil nadu", "TN"),
    StateModel("telangana", "TL"),
    StateModel("tripura", "TR"),
    StateModel("uttar pradesh", "UP"),
    StateModel("uttarakhand", "UK"),
    StateModel("west bengal", "WB")
)
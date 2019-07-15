package com.delhivery.orion.data

import com.delhivery.orion.utils.extensions.safeSubstring
import com.google.gson.annotations.SerializedName

data class StateModel(
  @SerializedName("state") val state: String,
  @SerializedName("state_id") val stateId: String,
  var checked: Boolean = false
) : BaseKeyTypeModel<String>() {

  override fun key() = stateId

  companion object {

    /**
     * State id from state name
     */
    fun idFromName(name: String) =
      StateModelList
          .filter { it.state.contains(name, true) }
          .firstOrNull()
          ?.stateId ?: name.safeSubstring(0, 3)
  }

  fun stateString() = "${state}(${stateId})"

  override fun equals(other: Any?): Boolean {
    return (other as StateModel).state.toLowerCase()
        .equals(state.toLowerCase())
  }

  override fun hashCode(): Int {
    return state.toLowerCase()
        .hashCode()
  }
}

val StateModelList = listOf<StateModel>(
    StateModel("Andaman & nicobar islands", "AN"),
    StateModel("Andhra pradesh", "AP"),
    StateModel("Arunachal pradesh", "AR"),
    StateModel("Assam", "AS"),
    StateModel("Bihar", "BR"),
    StateModel("Chandigarh", "CH"),
    StateModel("Chhattisgarh", "CG"),
    StateModel("Dadra & nagar haveli", "DH"),
    StateModel("Daman & diu", "DD"),
    StateModel("Delhi", "DL"),
    StateModel("Goa", "GA"),
    StateModel("Gujarat", "GJ"),
    StateModel("Haryana", "HR"),
    StateModel("Himachal pradesh", "HP"),
    StateModel("Jammu & kashmir", "JK"),
    StateModel("Jharkhand", "JH"),
    StateModel("Karnataka", "KA"),
    StateModel("Kerala", "KL"),
    StateModel("Madhya pradesh", "MP"),
    StateModel("Maharashtra", "MH"),
    StateModel("Meghalaya", "ML"),
    StateModel("Mizoram", "MZ"),
    StateModel("Nagaland", "NL"),
    StateModel("Odisha", "OR"),
    StateModel("Puducherry", "PY"),
    StateModel("Punjab", "PB"),
    StateModel("Rajasthan", "RJ"),
    StateModel("Sikkim", "SK"),
    StateModel("Tamil nadu", "TN"),
    StateModel("Telangana", "TL"),
    StateModel("Tripura", "TR"),
    StateModel("Uttar pradesh", "UP"),
    StateModel("Uttarakhand", "UK"),
    StateModel("West bengal", "WB")
)
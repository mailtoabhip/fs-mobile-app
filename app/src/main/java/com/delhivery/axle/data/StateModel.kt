package com.delhivery.axle.data

import com.google.gson.annotations.SerializedName

data class StateModel(
  @SerializedName("state") val state: String,
  @SerializedName("state_id") val stateId: String,
  @SerializedName("gn_state_code") val gnStateCode: String,
  var checked: Boolean = false
) : BaseKeyTypeModel<String>() {

  override fun key() = stateId

  companion object {

    /**
     * State id from state name
     */
    fun idFromName(name: String): String {
      val state = StateModelList.firstOrNull { it.state.contains(name, true) }
      return state?.stateId ?: state?.state?.substring(0, 3) ?: ""
    }
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

val StateModelList = listOf(
    StateModel("Andaman & Nicobar Islands", "AN", "AN"),
    StateModel("Andhra pradesh", "AP", "AP"),
    StateModel("Arunachal pradesh", "AR", "AR"),
    StateModel("Assam", "AS", "AS"),
    StateModel("Bihar", "BR", "BR"),
    StateModel("Chandigarh", "CH", "CH"),
    StateModel("Chhattisgarh", "CG", "CG"),
    StateModel("Dadra & nagar haveli", "DH", "DH DN"),
    StateModel("Daman & diu", "DD", "DD"),
    StateModel("Delhi", "DL", "DL"),
    StateModel("Goa", "GA", "GA"),
    StateModel("Gujarat", "GJ", "GJ"),
    StateModel("Haryana", "HR", "HR"),
    StateModel("Himachal pradesh", "HP", "HP"),
    StateModel("Jammu & kashmir", "JK", "JK"),
    StateModel("Jharkhand", "JH", "JH"),
    StateModel("Karnataka", "KA", "KA"),
    StateModel("Kerala", "KL", "KL"),
    StateModel("Lakhswadeep", "LD", "LK LD"),
    StateModel("Madhya pradesh", "MP", "MP"),
    StateModel("Maharashtra", "MH", "MH"),
    StateModel("Manipur", "MN", "MN"),
    StateModel("Meghalaya", "ML", "ML"),
    StateModel("Mizoram", "MZ", "MZ"),
    StateModel("Nagaland", "NL", "NL"),
    StateModel("Odisha", "OR", "OR"),
    StateModel("Puducherry", "PY", "PU"),
    StateModel("Punjab", "PB", "PB"),
    StateModel("Rajasthan", "RJ", "RJ"),
    StateModel("Sikkim", "SK", "SK"),
    StateModel("Tamil nadu", "TN", "TN"),
    StateModel("Telangana", "TL", "TS"),
    StateModel("Tripura", "TR", "TR"),
    StateModel("Uttar pradesh", "UP", "UP"),
    StateModel("Uttarakhand", "UK", "UK"),
    StateModel("West bengal", "WB", "WB")
)

data class StateModelForDeletingRoutes(
    @SerializedName("state") val state: String,
    @SerializedName("state_id") val stateId: String
)
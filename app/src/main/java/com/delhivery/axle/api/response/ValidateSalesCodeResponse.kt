package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class ValidateSalesCodeResponse (
    @SerializedName("valid")
    val isValid: Boolean,
    @SerializedName("sales_code")
    val salesCode: String,
    @SerializedName("agent_name")
    val agentName: String?,
    @SerializedName("agent_code")
    val agentCode: String?,
    @SerializedName("message")
    val message: String,

)
package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class FastagStatusResponse(
    @SerializedName("status")
    val status: String? = ""
) {
    fun isBlacklisted(): Boolean = status.equals("Blacklist", ignoreCase = true) || status.equals("Blacklisted", ignoreCase = true)
}

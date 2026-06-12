package com.dfd.delfin.api.request

import com.google.gson.annotations.SerializedName

data class ValidateSalesRequest (
    @SerializedName("sales_code")
    var salesCode: String
)
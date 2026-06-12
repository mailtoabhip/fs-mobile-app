package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class RcProcessResponse(
    @SerializedName("job_id")
    val jobId: String?,
    @SerializedName("status")
    val status: String?
)

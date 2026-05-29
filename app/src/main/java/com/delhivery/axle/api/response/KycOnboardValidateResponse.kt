package com.delhivery.axle.api.response

import com.google.gson.annotations.SerializedName

data class KycOnboardValidateResponse(
    @SerializedName("fastag_customer_exists")
    val fastagCustomerExists: Boolean,
    @SerializedName("kyc_type")
    val kycType: String?,
    @SerializedName("is_kyc_upgrade_eligible")
    val isKycUpgradeEligible: Boolean?
)

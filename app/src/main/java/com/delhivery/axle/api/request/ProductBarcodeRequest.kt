package com.delhivery.axle.api.request

import com.google.gson.annotations.SerializedName

data class ProductBarcodeRequest(
    @SerializedName("journey_id")
    val journeyId: String,

    @SerializedName("barcode")
    val barcode: String
)

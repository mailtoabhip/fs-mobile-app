package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

data class ProductBarcodeResponse(
    @SerializedName("products")
    val products: Any?,

    @SerializedName("org_mobility_id")
    val orgMobilityId: String?,

    @SerializedName("barcodes")
    val barcodes: List<BarcodeItem>?
)

data class BarcodeItem(
    @SerializedName("barcode")
    val barcode: String?,

    @SerializedName("tagId")
    val tagId: String?
)

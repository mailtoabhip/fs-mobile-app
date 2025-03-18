package com.delhivery.axle.api.response

import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.google.gson.annotations.SerializedName


data class PlacementsLoadDataResponse(
    @SerializedName("ftl_adhoc") val ftlAdhoc: List<HomePlacementsItemData>,
    @SerializedName("ftl_regular") val ftlRegular: List<HomePlacementsItemData>,
    @SerializedName("intracity_adhoc") val intracityAdhoc: List<HomePlacementsItemData>,
    @SerializedName("intracity_regular") val intracityRegular: List<HomePlacementsItemData>
)
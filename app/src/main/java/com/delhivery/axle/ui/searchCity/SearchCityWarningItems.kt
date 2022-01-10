package com.delhivery.axle.ui.searchCity

import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.data.search.SearchWarningItemData

val SearchCityWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "No city found",
        "Please change the search parameters",
        "REFRESH", SearchWarningAction_NoResult
    )
)
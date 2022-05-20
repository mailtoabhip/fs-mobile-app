package com.delhivery.axle.ui.searchcitystate

import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.data.search.SearchWarningItemData

val SearchCityStateWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "Search result not found",
        "Please change the search parameters",
        "REFRESH", SearchWarningAction_NoResult
    )
)
package com.dfd.delfin.ui.searchCity

import com.dfd.delfin.data.search.SearchWarningAction_NoResult
import com.dfd.delfin.data.search.SearchWarningItemData

val SearchCityWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "No city found",
        "Please change the search parameters",
        "REFRESH", SearchWarningAction_NoResult
    )
)
package com.dfd.delfin.ui.searchcitystate

import com.dfd.delfin.data.search.SearchWarningAction_NoResult
import com.dfd.delfin.data.search.SearchWarningItemData

val SearchCityStateWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "Search result not found",
        "Please change the search parameters",
        "REFRESH", SearchWarningAction_NoResult
    )
)
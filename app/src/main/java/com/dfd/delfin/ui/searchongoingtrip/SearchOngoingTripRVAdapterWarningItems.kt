package com.dfd.delfin.ui.searchongoingtrip

import com.dfd.delfin.data.search.SearchWarningAction_NoResult
import com.dfd.delfin.data.search.SearchWarningItemData

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 13/5/21
 */

val SearchOngoingTripWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "No trips found",
        "Please change the search parameters",
        "RESET", SearchWarningAction_NoResult
    )
)

val SearchOngoingTripsWarningItem_NoResult = SearchWarningItem(
    SearchWarningItemData(
        "No trips found",
        "Please change the search parameters",
        "REFRESH", SearchWarningAction_NoResult
    )
)
package com.dfd.delfin.ui.searchtrip

import com.dfd.delfin.data.search.SearchWarningAction_NoResult
import com.dfd.delfin.data.search.SearchWarningItemData

/**
 * No bids warning item, when no bids are found
 *
 * @Zeplin https://zpl.io/2pvmPol
 */
val SearchWarningItem_NoResult = SearchWarningItem(
  SearchWarningItemData(
    "No trips found",
    "Please change the search parameters",
    "RESET", SearchWarningAction_NoResult
  )
)

val SearchTripsWarningItem_NoResult = SearchWarningItem(
  SearchWarningItemData(
    "No trips found",
    "Please change the search parameters",
    "REFRESH", SearchWarningAction_NoResult
  )
)


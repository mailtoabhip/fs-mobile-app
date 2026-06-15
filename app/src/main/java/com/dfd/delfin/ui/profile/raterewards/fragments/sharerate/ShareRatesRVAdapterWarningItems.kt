package com.dfd.delfin.ui.profile.raterewards.fragments.sharerate

import com.dfd.delfin.data.sharerates.ShareRatesTimeOutAction
import com.dfd.delfin.data.sharerates.ShareRatesWarningAction_NoRates
import com.dfd.delfin.data.sharerates.ShareRatesWarningItemData

/**
 * No Rates warning item
 */
val ShareRatesWarningItem_NoRate = ShareRatesWarningItem(
    ShareRatesWarningItemData(
        "No Active Offers",
        "",
        "", ShareRatesWarningAction_NoRates
    )
)

/**
 * Rates timeout item
 */
val ShareRatesWarningItem_TimeOut = ShareRatesWarningItem(
    ShareRatesWarningItemData(
        "Session timed out!",
        "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
        "REFRESH", ShareRatesTimeOutAction
    )
)


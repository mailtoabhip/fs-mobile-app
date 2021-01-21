package com.delhivery.axle.ui.ledger

import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutAction
import com.delhivery.axle.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.axle.data.home.loads.HomeLoadsWarningItemData

val ConsolidatedWarningItem_NoLedger = ConsolidatedPageWarningItem(
        HomeLoadsWarningItemData(
                "No Ledgers found",
                "Please try again after some time",
                "REFRESH", HomeLoadsWarningAction_NoLoads
        )
)

val ConsolidatedWarningItem_TimeOut = ConsolidatedPageWarningItem(
        HomeLoadsWarningItemData(
                "Session timed out!",
                "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
                "REFRESH", HomeLoadsTimeOutAction
        )
)
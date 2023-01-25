package com.delhivery.axle.ui.home.fragments.contracts

import com.delhivery.axle.data.home.contracts.HomeContractsTimeOutAction
import com.delhivery.axle.data.home.contracts.HomeContractsWarningAction_NoLoads
import com.delhivery.axle.data.home.contracts.HomeContractsWarningItemData

val HomeContractsWarningItem_NoLoads = HomeContractsWarningItem(
  HomeContractsWarningItemData(
    "No Contracts found",
    " ",
    "REFRESH", HomeContractsWarningAction_NoLoads
  )
)

val HomeContractsWarningItem_TimeOut = HomeContractsWarningItem(
  HomeContractsWarningItemData(
    "Session timed out!",
    "Unfortunately, we couldn't fetch the data you are looking for. \n Kindly refresh.",
    "REFRESH", HomeContractsTimeOutAction
  )
)
package com.dfd.delfin.ui.home.fragments.contracts

import com.dfd.delfin.data.home.contracts.HomeContractsTimeOutAction
import com.dfd.delfin.data.home.contracts.HomeContractsWarningAction_NoLoads
import com.dfd.delfin.data.home.contracts.HomeContractsWarningItemData

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
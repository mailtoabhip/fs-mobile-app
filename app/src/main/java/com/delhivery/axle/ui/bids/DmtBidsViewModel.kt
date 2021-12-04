package com.delhivery.axle.ui.bids

import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class DmtBidsViewModel @Inject constructor(
    private val bidsRepository: BidsRepository,
    private val transactionsRepository: TransactionsRepository
): BaseViewModel() {




}
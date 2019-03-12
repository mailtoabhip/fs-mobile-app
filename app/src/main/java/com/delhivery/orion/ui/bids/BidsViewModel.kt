package com.delhivery.orion.ui.bids

import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.ui.bids.BidType.Unknown
import javax.inject.Inject

class BidsViewModel @Inject constructor() : BaseViewModel() {

  /* bid type */
  var bidType: BidType = Unknown
}
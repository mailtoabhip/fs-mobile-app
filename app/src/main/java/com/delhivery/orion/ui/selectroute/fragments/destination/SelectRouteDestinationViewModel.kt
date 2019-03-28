package com.delhivery.orion.ui.selectroute.fragments.destination

import com.delhivery.orion.repository.OrionDataRepository
import com.delhivery.orion.ui.base.BaseViewModel
import javax.inject.Inject

class SelectRouteDestinationViewModel @Inject constructor(
  private val originDataRepository: OrionDataRepository
) : BaseViewModel() {
}
package com.delhivery.orion.ui.home.fragments.alerts

import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeAlertsBinding
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment

class HomeAlertsFragment : HomeBaseFragment<FragmentHomeAlertsBinding, HomeAlertsViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: HomeAlertsFragment by lazy { HomeAlertsFragment() }
  }

  override fun getViewModelClass() = HomeAlertsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_alerts
}
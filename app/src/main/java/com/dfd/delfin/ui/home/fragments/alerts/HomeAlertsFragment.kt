package com.dfd.delfin.ui.home.fragments.alerts

import com.dfd.delfin.R
import com.dfd.delfin.databinding.FragmentHomeAlertsBinding
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment

class HomeAlertsFragment : HomeBaseFragment<FragmentHomeAlertsBinding, HomeAlertsViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: HomeAlertsFragment by lazy { HomeAlertsFragment() }
  }

  override fun getViewModelClass() = HomeAlertsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_alerts
}
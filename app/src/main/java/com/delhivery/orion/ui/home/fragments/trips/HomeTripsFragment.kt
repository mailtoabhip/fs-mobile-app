package com.delhivery.orion.ui.home.fragments.trips

import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeTripsBinding
import com.delhivery.orion.ui.base.BaseFragment

class HomeTripsFragment : BaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: HomeTripsFragment by lazy { HomeTripsFragment() }
  }

  override fun getViewModelClass() = HomeTripsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_trips

}
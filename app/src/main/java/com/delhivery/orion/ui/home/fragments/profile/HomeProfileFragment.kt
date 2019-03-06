package com.delhivery.orion.ui.home.fragments.profile

import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeProfileBinding
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment

class HomeProfileFragment : HomeBaseFragment<FragmentHomeProfileBinding, HomeProfileViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: HomeProfileFragment by lazy { HomeProfileFragment() }
  }

  override fun getViewModelClass() = HomeProfileViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_profile

}
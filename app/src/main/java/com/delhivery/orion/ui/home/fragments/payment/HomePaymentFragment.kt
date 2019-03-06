package com.delhivery.orion.ui.home.fragments.payment

import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomePaymentBinding
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment

class HomePaymentFragment : HomeBaseFragment<FragmentHomePaymentBinding, HomePaymentViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: HomePaymentFragment by lazy { HomePaymentFragment() }
  }

  override fun getViewModelClass() = HomePaymentViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_payment
}
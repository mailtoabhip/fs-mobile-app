package com.delhivery.axle.ui.home.fragments.payment

import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomePaymentBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment

class HomePaymentFragment : HomeBaseFragment<FragmentHomePaymentBinding, HomePaymentViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: HomePaymentFragment by lazy { HomePaymentFragment() }
  }

  override fun getViewModelClass() = HomePaymentViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_payment
}
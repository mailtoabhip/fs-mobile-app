package com.delhivery.axle.ui.profile.raterewards.fragments

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel

abstract class ShareRateGetRewardsBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {


  init {
    hasInlineProgress = true
  }

  /**
   * refresh screen data
   */
  abstract fun refreshData()
}
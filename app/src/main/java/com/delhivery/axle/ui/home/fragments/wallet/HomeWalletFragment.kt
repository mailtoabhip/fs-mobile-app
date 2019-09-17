package com.delhivery.axle.ui.home.fragments.wallet

import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeWalletBinding
import com.delhivery.axle.ui.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.utils.DialogUtils
import javax.inject.Inject

class HomeWalletFragment : HomeBaseFragment<FragmentHomeWalletBinding, HomeWalletViewModel>(),
    TitleProvider {

  override val title: CharSequence
    get() = "Balance"

  init {
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeWalletFragment by lazy { HomeWalletFragment() }
  }

  @Inject lateinit var dialogUtils: DialogUtils

  override fun getViewModelClass() = HomeWalletViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_wallet

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

  }

}
package com.delhivery.axle.ui.home.fragments.wallet

import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.FragmentHomeWalletBinding
import com.delhivery.axle.databinding.ViewWalletIntroBinding
import com.delhivery.axle.ui.home.activity.bank.bankTransferIntent
import com.delhivery.axle.ui.home.activity.fuel.tripsFuelCreditIntent
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.wallet.viewpager.WalletIntroConfig
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.REQCODE_BANK_TRANSACTION
import com.delhivery.axle.utils.REQCODE_CREATE_ACTIVE_TRIPS
import com.delhivery.axle.utils.REQCODE_WALLET_ONBOARDING
import javax.inject.Inject

/**
 * Handles user wallet view
 */
class HomeWalletFragment : HomeBaseFragment<FragmentHomeWalletBinding, HomeWalletViewModel>(),
    TitleProvider {

  override val title: CharSequence
    get() = "Axle Money"

  init {
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeWalletFragment by lazy { HomeWalletFragment() }
  }

  @Inject lateinit var dialogUtils: DialogUtils

  private val adapter by lazy {
    IntroPagerAdapter()
  }

  override fun getViewModelClass() = HomeWalletViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_wallet

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.active = viewModel.walletActivated
    binding.error = false
    binding.loading = true
    binding.executePendingBindings()

    adapter.layoutInflater = layoutInflater
    binding.viewPager.apply {
      this.adapter = this@HomeWalletFragment.adapter
      binding.pagerIndicator.viewPager = this
    }

    viewModel.walletLiveData.reobserve(this, Observer {
      binding.loading = false
      if (it != null) {
        binding.error = false
        binding.active = !it.autoWithdraw
        viewModel.walletActivated = !it.autoWithdraw
        binding.textBalance.text = it.balance()
      } else {
        binding.error = true
        binding.containerError.title = "Session Timed out"
        binding.containerError.subTitle =
          "Unfortunately, we couldn't fetch the data you are looking for. \n" +
              " Kindly refresh."
        binding.containerError.actionLabel = "REFRESH"
      }
      binding.executePendingBindings()
    })

    binding.containerFuel.setOnClickListener { openFuelCredits() }
    binding.containerBank.setOnClickListener { openBankTransfer() }
    binding.containerFastTag.setOnClickListener { openFastTag() }
    binding.btnActivate.setOnClickListener { moveNext() }
    binding.refreshWallet.setOnRefreshListener {
      binding.refreshWallet.isRefreshing = false
      binding.loading = true
      binding.executePendingBindings()
      viewModel.fetchWalletData()
    }
    binding.containerBalance.setOnClickListener { openTransactions() }
    binding.containerError.btnAction.setOnClickListener {
      binding.loading = true
      binding.executePendingBindings()
      viewModel.activateWallet()
    }

    viewModel.fetchWalletData()
  }

  private fun moveNext() {
    val currentPage = binding.viewPager.currentItem
    if (currentPage < adapter.count - 1) {
      binding.viewPager.setCurrentItem(currentPage + 1, true)
    } else {
      context?.let {
        startActivityForResult(
            walletOnbaordingIntent(it), REQCODE_WALLET_ONBOARDING
        )
      }
    }
  }

  private fun openTransactions() {
    context?.let { navigationUtils.navigate(transactionsIntent(it)) }
  }

  private fun openFuelCredits() {
    context?.let {
      startActivityForResult(
          tripsFuelCreditIntent(it, viewModel.optinDate), REQCODE_CREATE_ACTIVE_TRIPS
      )
    }
  }

  private fun openBankTransfer() {
    context?.let { startActivityForResult(bankTransferIntent(it), REQCODE_BANK_TRANSACTION) }
  }

  private fun openFastTag() {
    uiUtils.showToast(getString(string.label_coming_soon))
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK) {
      when (requestCode) {
        REQCODE_BANK_TRANSACTION, REQCODE_CREATE_ACTIVE_TRIPS -> {
          binding.refreshWallet.isRefreshing = false
          binding.loading = true
          binding.executePendingBindings()
          viewModel.fetchWalletData()
        }

        REQCODE_WALLET_ONBOARDING -> {
          binding.loading = true
          binding.executePendingBindings()
          viewModel.activateWallet()
        }
      }
    } else if (resultCode == RESULT_FIRST_USER) {
      if (requestCode == REQCODE_CREATE_ACTIVE_TRIPS) {
        action(NavigateHomeFragmentAction(LoadsFragment))
      }
    }
  }

  /**
   * Pager adapter for WalletIntro
   */
  inner class IntroPagerAdapter : PagerAdapter() {

    lateinit var layoutInflater: LayoutInflater

    override fun isViewFromObject(
      p0: View,
      p1: Any
    ) = p0 == p1

    override fun getCount() = WalletIntroConfig.size

    override fun instantiateItem(
      container: ViewGroup,
      position: Int
    ) = ViewWalletIntroBinding.inflate(layoutInflater, container, true).apply {
      val item = WalletIntroConfig[position]
      title = item.title
      message = item.message
      icon = item.icon
    }.root

    override fun destroyItem(
      container: ViewGroup,
      position: Int,
      obj: Any
    ) = if (obj is View) {
      container.removeView(obj)
    } else {
      super.destroyItem(container, position, obj)
    }
  }

}
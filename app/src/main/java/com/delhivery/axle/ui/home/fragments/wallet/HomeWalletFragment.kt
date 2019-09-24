package com.delhivery.axle.ui.home.fragments.wallet

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
import com.delhivery.axle.ui.home.fragments.wallet.viewpager.WalletIntroConfig
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.REQCODE_BANK_TRANSACTION
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

    if (viewModel.walletActivated) {
      binding.refreshWallet.visibility = View.VISIBLE
      binding.containerActivate.visibility = View.GONE
      binding.refreshWallet.isRefreshing = true
    } else {
      binding.refreshWallet.visibility = View.GONE
      binding.containerActivate.visibility = View.VISIBLE
      binding.refreshWallet.isRefreshing = false
    }

    adapter.layoutInflater = layoutInflater
    binding.viewPager.apply {
      this.adapter = this@HomeWalletFragment.adapter
      binding.pagerIndicator.viewPager = this
    }

    viewModel.walletLiveData.reobserve(this, Observer {
      when {
        it.autoWithdraw -> {
          binding.refreshWallet.isRefreshing = false
          binding.containerActivate.visibility = View.VISIBLE
          binding.refreshWallet.visibility = View.GONE
          binding.containerWallet.visibility = View.GONE
        }
        else -> {
          viewModel.walletActivated = true
          binding.textBalance.text = it.balance()
          binding.refreshWallet.isRefreshing = false
          binding.containerActivate.visibility = View.GONE
          binding.refreshWallet.visibility = View.VISIBLE
          binding.containerWallet.visibility = View.VISIBLE
        }
      }
    })

    viewModel.progressLiveData.reobserve(this, Observer {
      if (it) {
        binding.refreshWallet.visibility = View.VISIBLE
        binding.containerWallet.visibility = View.GONE
        binding.refreshWallet.isRefreshing = true
        binding.containerActivate.visibility = View.GONE
      }
    })

    binding.viewTransactionHistory.setOnClickListener { openTransactions() }
    binding.containerFuel.setOnClickListener { openFuelCredits() }
    binding.containerBank.setOnClickListener { openBankTransfer() }
    binding.containerFastTag.setOnClickListener { openFastTag() }
    binding.btnActivate.setOnClickListener { viewModel.activateWallet() }
    binding.refreshWallet.setOnRefreshListener { viewModel.fetchWalletData() }

    viewModel.fetchWalletData()
  }

  private fun openTransactions() {
    context?.let { navigationUtils.navigate(transactionsIntent(it)) }
  }

  private fun openFuelCredits() {
    context?.let { navigationUtils.navigate(tripsFuelCreditIntent(it)) }
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
    if (requestCode == REQCODE_BANK_TRANSACTION && resultCode == RESULT_OK) {
      viewModel.fetchWalletData()
    }
  }

  class IntroPagerAdapter : PagerAdapter() {

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
package com.delhivery.axle.ui.home.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityWalletOnboardingBinding
import com.delhivery.axle.ui.base.BaseActivity

/**
 * Handles wallet process onboarding
 */
class WalletOnboardingActivity : BaseActivity<ActivityWalletOnboardingBinding, WalletOnboardingViewModel>() {

  override fun getViewModelClass() = WalletOnboardingViewModel::class.java

  override fun layoutId() = R.layout.activity_wallet_onboarding

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Axle Wallet Flow"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.cbUnderstand.setOnCheckedChangeListener { v, selected ->
      v.post {
        binding.btnActivate.isEnabled = selected
      }
    }
    binding.btnActivate.isEnabled = false

    binding.btnActivate.setOnClickListener {
      setResult(Activity.RESULT_OK)
      finish()
    }
  }
}

/**
 * Bank Transfer intent
 */
fun walletOnbaordingIntent(
  context: Context
) = Intent(context, WalletOnboardingActivity::class.java).apply {
}
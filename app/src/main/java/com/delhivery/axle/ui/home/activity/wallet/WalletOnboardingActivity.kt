package com.delhivery.axle.ui.home.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityWalletOnboardingBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Handles wallet process onboarding
 */
class WalletOnboardingActivity : BaseActivity<ActivityWalletOnboardingBinding, WalletOnboardingViewModel>() {

  @Inject lateinit var userPrefs : UserPrefs

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

  override fun onBackPressed() {
    userPrefs.setPreviousScreen(this.javaClass.name)
    super.onBackPressed()
  }
}

/**
 * Bank Transfer intent
 */
fun walletOnbaordingIntent(
  context: Context
) = Intent(context, WalletOnboardingActivity::class.java).apply {
}
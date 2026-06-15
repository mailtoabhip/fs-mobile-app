package com.dfd.delfin.ui.home.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityWalletOnboardingBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Handles wallet process onboarding
 */
class WalletOnboardingActivity : BaseActivity<ActivityWalletOnboardingBinding, WalletOnboardingViewModel>() {

  @Inject lateinit var userPrefs : UserPrefs
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  override fun getViewModelClass() = WalletOnboardingViewModel::class.java

  override fun layoutId() = R.layout.activity_wallet_onboarding

  override fun requireConnection() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("WalletOnboardingActivity_SetupTime")
    activitySetupTrace?.start()
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
    title = "Axle Wallet Flow"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })

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

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
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
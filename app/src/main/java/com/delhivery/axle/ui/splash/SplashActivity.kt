package com.delhivery.axle.ui.splash

import android.graphics.Color
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.HomeActivity
import com.delhivery.axle.ui.onboarding.OnboardingActivity
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.ui.splash.SplashPostState.Onboarding
import com.github.florent37.kotlin.pleaseanimate.please

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SplashViewModel::class.java

  override fun layoutId() = R.layout.activity_splash

  override fun requireConnection() = false

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* start splash animation */
    animate()
  }

  /**
   * Splash animation chain
   */
  private fun animate() {
    val isAuthenticated = viewModel.postState()
    please(1500, OvershootInterpolator()) {
      animate(binding.textDelhivery) toBe {
        alpha(1f)
      }
      animate(binding.imgLogo) toBe {
        alpha(1f)
        scale(1.6f, 1.6f)
      }
    }.withEndAction {
      postAnimate(isAuthenticated)
    }
        .setStartDelay(SplashAnimationDelay / 2)
        .start()
  }

  private fun postAnimate(state: SplashPostState) {
    when (state) {
      Onboarding -> OnboardingActivity::class
      Auth -> AuthenticationActivity::class
      Home -> HomeActivity::class
    }.let {
      navigationUtils.navigate(it.java, true)
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L
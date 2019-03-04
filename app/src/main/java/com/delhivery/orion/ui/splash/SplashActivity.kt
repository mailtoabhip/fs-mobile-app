package com.delhivery.orion.ui.splash

import android.graphics.Color
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivitySplashBinding
import com.delhivery.orion.ui.auth.AuthenticationActivity
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeActivity
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
    please(1500, OvershootInterpolator()) {
      animate(binding.imgLogo) toBe {
        toBeRotated(360f)
      }
      animate(binding.imgLogo) toBe {
        aboveOf(binding.centerDot, marginDp = 12f)
      }
      animate(binding.imgName) toBe {
        alpha(1f)
        belowOf(binding.centerDot, marginDp = 12f)
      }
      animate(binding.textDelhivery) toBe {
        alpha(1f)
      }
    }.withEndAction {
      please(100L) {
        animate(binding.viewCenterReveal) toBe {
          sameCenterAs(binding.imgLogo)
        }
      }.withEndAction {
        please {
          animate(binding.viewCenterReveal) toBe {
            scale(1f, 1f)
          }
        }.withEndAction {
          postAnimate()
        }
            .start()
      }
          .setStartDelay(SplashAnimationDelay / 2)
          .start()
    }
        .setStartDelay(SplashAnimationDelay)
        .start()
  }

  private fun postAnimate() {
    if (viewModel.authState()) {
      navigationUtils.navigate(SelectRouteWelcomeActivity::class.java, finishAfter = true)
    } else {
      navigationUtils.navigate(AuthenticationActivity::class.java, finishAfter = true)
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L
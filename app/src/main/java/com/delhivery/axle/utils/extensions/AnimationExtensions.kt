package com.delhivery.axle.utils.extensions

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.delhivery.axle.R
import com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText
import com.github.florent37.kotlin.pleaseanimate.please

/**
 * Show error vibration with error state for [DelhiveryOTPViewEditText]
 */
fun View.errorVibrate(start: Boolean = true): ObjectAnimator = resources.getDimension(R.dimen.distance_otp_vibrate)
    .let { distance ->
      ObjectAnimator.ofFloat(
          this, "translationX", 0f, distance, -distance, distance, -distance, 0f
      )
          .apply {
            if (this@errorVibrate is DelhiveryOTPViewEditText) {
              addListener(object : AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                  /* error disabled */
                  this@errorVibrate.error = false
                }

                override fun onAnimationCancel(animation: Animator) {
                  /* error disabled */
                  this@errorVibrate.error = false
                }

                override fun onAnimationStart(animation: Animator) {
                  /* error enabled */
                  this@errorVibrate.error = true
                }
              })
            }
            /* start on demand, by default start */
            if (start) {
              start()
            }
          }
    }

/**
 * Fade animation
 */
fun View.fadeAnim(
  infinite: Boolean = true,
  start: Boolean = true
) = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f, 1f)
    .apply {
      duration = 1500
      if (infinite) {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
      }
      interpolator = FastOutSlowInInterpolator()
      addListener(object : AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {}

        override fun onAnimationCancel(animation: Animator) {
          /* reset view alpha to 1 */
          please { animate(this@fadeAnim) toBe { visible() } }.start()
        }

        override fun onAnimationStart(animation: Animator) {}
      })
      if (start) {
        start()
      }
    }
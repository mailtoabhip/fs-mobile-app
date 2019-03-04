package com.delhivery.orion.ui.custom

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View

class DelhiveryOnboardingArcView(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {

  /* arc color */
  private val ArcColor = Color.WHITE
  private val HeightArcFactor = 1.5f

  /* arc radius for animation */
  private var arcRadius = 0f

  /* arc paint */
  private val arcPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ArcColor
    }
  }

  override fun onDraw(canvas: Canvas?) {
    canvas?.apply {
      //      val radius = height * 1.5f
      canvas.drawCircle(width / 2f, height * HeightArcFactor, arcRadius, arcPaint)
    }
  }

  /**
   * Animate open/ center reveal
   */
  fun animateOpen(
    startDelay: Long = 300,
    endAction: () -> Unit
  ) {
    ValueAnimator.ofFloat(0.5f, HeightArcFactor)
        .apply {
          duration = 300
          interpolator = FastOutSlowInInterpolator()
          addListener(object : AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
              endAction()
            }

            override fun onAnimationCancel(animation: Animator?) {
              endAction()
            }

            override fun onAnimationStart(animation: Animator?) {
            }
          })
          addUpdateListener {
            arcRadius = height * (it.animatedValue as Float)
            invalidate()
          }
          setStartDelay(startDelay)
          start()
        }
  }
}
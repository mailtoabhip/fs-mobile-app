package com.dfd.delfin.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.dfd.delfin.R

/**
 * Shimmer layout
 */
class DelfinShimmerLayout(
  context: Context,
  attrs: AttributeSet?
) : FrameLayout(context, attrs) {

  init {
    animateShimmer()
  }

  /* shimmer effect width */
  private val ShimmerWidth by lazy {
    resources.getDimension(R.dimen.size_24dp)
  }

  /* shimmer paint */
  private val shimmerPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = LinearGradient(
          0f, 0f, ShimmerWidth, height * 1f,
          intArrayOf(Color.TRANSPARENT, ShimmerColor, Color.TRANSPARENT),
          floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.MIRROR
      )
    }
  }

  private var factor = 0.0f

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)
    canvas.apply {
      save()
      rotate(ShimmerTiltAngle, width / 2f, height / 2f)
      val x = width * factor
      drawRect(x, -50f, x + ShimmerWidth, height * 1f, shimmerPaint)
      restore()
    }
  }

  private fun animateShimmer() {
    ValueAnimator.ofFloat(-0.5f, 1f)
        .apply {
          duration = ShimmerCycleTime
          repeatCount = ValueAnimator.INFINITE
          repeatMode = ValueAnimator.RESTART
          interpolator = FastOutSlowInInterpolator()
          addUpdateListener {
            factor = (it.animatedValue as Float)
            invalidate()
          }
        }
        .start()
  }
}

/* shimmer color */
private val ShimmerColor = Color.parseColor("#edffffff")
/* shimmer cycle time */
private const val ShimmerCycleTime = 1000L //ms
/* shimmer tilt angle */
private const val ShimmerTiltAngle = 5f //degress
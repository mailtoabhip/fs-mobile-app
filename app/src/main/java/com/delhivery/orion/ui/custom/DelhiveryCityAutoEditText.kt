package com.delhivery.orion.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import com.delhivery.orion.R

class DelhiveryCityAutoEditText(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatAutoCompleteTextView(context, attrs) {

  val RightGap = context.resources.getDimension(R.dimen.size_12dp)
  val DotsGap = context.resources.getDimension(R.dimen.size_6dp)
  val DotRadius = context.resources.getDimension(R.dimen.size_2dp)
  val MaxTransY = context.resources.getDimension(R.dimen.size_8dp)

  val paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
      alpha = 100
    }
  }

  /* animation factor */
  private var factor = 0.0f

  private var progress = false

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)

    if (progress) {
      canvas?.apply {
        val pos = factor.toInt()
        val transFactor = factor % 1
        for (i in 0 until DotsCount) {
          val cx = width - RightGap - DotRadius - i * DotsGap
          val cy = if (i == pos) {
            height / 2f - 2f * MaxTransY * if (transFactor > 0.5f) 1f - transFactor else transFactor
          } else {
            height / 2f
          }
          drawCircle(cx, cy, DotRadius, paint)
        }
      }
    }
  }

  fun progress(start: Boolean = true) {
    if (start == progress) return
    progress = start
    if (progress) {
      _anim.start()
    } else {
      _anim.cancel()
      invalidate()
    }
  }

  private val _anim by lazy {
    ValueAnimator.ofFloat(0.0f, DotsCount.toFloat())
        .apply {
          duration = 1000
          repeatMode = ValueAnimator.RESTART
          repeatCount = ValueAnimator.INFINITE
          interpolator = AccelerateDecelerateInterpolator()
          addUpdateListener {
            factor = (it.animatedValue as Float)
            invalidate()
          }
          start()
        }
  }
}

private const val DotsCount = 3
package com.delhivery.axle.ui.custom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.delhivery.axle.R

/**
 * Delhivery style Progress view
 *
 * #Zeplin: https://app.zeplin.io/project/5c77996f826def6250138e25/screen/5c779b3479fda9bda447890f
 */
class DelhiveryProgressView(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  private val RotatingBallRadius by lazy {
    resources.getDimension(
        R.dimen.delhivery_progress_view_rotation_ball_radius
    )
  }
  private val OrbitTrackThickness by lazy {
    resources.getDimension(
        R.dimen.delhivery_progress_view_orbit_thickness
    )
  }
  private val RotatingBallColor = Color.parseColor("#ff3131")
  private val OrbitColor = Color.BLACK

  /* orbit paint */
  private val orbitPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      strokeWidth = OrbitTrackThickness
      color = OrbitColor
      style = STROKE
    }
  }

  private val rotatingBallPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = RotatingBallColor
    }
  }

  private val rotateAnim: ObjectAnimator by lazy {
    ObjectAnimator.ofFloat(
        this, "rotation", 0f, 360f
    )
        .apply {
          duration = 1500
          repeatMode = ValueAnimator.RESTART
          repeatCount = ValueAnimator.INFINITE
          interpolator = LinearInterpolator()
        }
  }

  override fun onDraw(canvas: Canvas) {
    canvas?.apply {
      /* Draw orbit */
      orbitPaint.apply {
        color = OrbitColor
        strokeWidth = OrbitTrackThickness
      }
      val orbitRadius = Math.min(width, height) / 2 - RotatingBallRadius
      drawCircle(width / 2f, height / 2f, orbitRadius, orbitPaint)

      /* draw rotating ball */
      drawCircle(width / 2f, RotatingBallRadius, RotatingBallRadius, rotatingBallPaint)

      /* draw rotating ball stroke */
      orbitPaint.apply {
        color = Color.WHITE
        strokeWidth = OrbitTrackThickness * 2
      }
      drawCircle(
          width / 2f, RotatingBallRadius,
          RotatingBallRadius + OrbitTrackThickness / 2,
          orbitPaint
      )
    }
  }

  fun startAnim() {
    if (!rotateAnim.isRunning) {
      rotateAnim.start()
    }
  }

  fun stopAnim() {
    rotateAnim.cancel()
  }
}
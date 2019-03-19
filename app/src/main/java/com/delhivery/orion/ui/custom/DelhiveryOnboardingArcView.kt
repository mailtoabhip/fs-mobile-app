package com.delhivery.orion.ui.custom

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.ui.custom.AnimationType.RevealOpen

class DelhiveryOnboardingArcView(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  private enum class ArcDirection(
    val dirId: Int,
    val factor: Float
  ) {
    Top(0, HeightArcFactor),
    Bottom(1, (1 - HeightArcFactor));

    companion object {
      fun byId(id: Int) = values().filter { it.dirId == id }.firstOrNull() ?: Top
    }
  }

  init {
    /* get attributes */
    attrs?.let { a ->
      val typedArray =
        context.obtainStyledAttributes(a, R.styleable.DelhiveryOnboardingArcView, 0, 0)
      direction =
          ArcDirection.byId(typedArray.getInt(R.styleable.DelhiveryOnboardingArcView_direction, 0))
      typedArray.recycle()
    }

  }

  /* arc direction */
  private lateinit var direction: ArcDirection

  /* arc color */
  private val ArcColor = Color.WHITE

  /* arc radius for animation */
  private var arcRadius = 0f

  /* arc paint */
  private val arcPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ArcColor
    }
  }
  /* shadow paint */
  private val shadowPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#1e000000")
      shader = RadialGradient(
          width / 2f, height * direction.factor, height * HeightArcFactor, Color.TRANSPARENT,
          Color.parseColor("#32000000"),
          Shader.TileMode.MIRROR
      )
    }
  }

  override fun onDraw(canvas: Canvas?) {
    canvas?.apply {
      canvas.drawCircle(width / 2f, height * direction.factor, arcRadius, shadowPaint)
      canvas.drawCircle(width / 2f, height * direction.factor - ShadowWidth, arcRadius, arcPaint)
    }
  }

  /**
   * Animate open/ center reveal
   */
  fun animate(
    type: AnimationType = RevealOpen,
    startDelay: Long = 300,
    endAction: () -> Unit
  ) {
    ValueAnimator.ofFloat(*type.values)
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

/* arc height animation factor */
private const val HeightArcFactor = 2f

private const val ShadowWidth = 4f

/* Arc animation type */
enum class AnimationType(vararg val values: Float) {
  RevealOpen(0.5f, HeightArcFactor),
  RevealCloseOpen(HeightArcFactor, 0.5f, HeightArcFactor),
  RevealClose(HeightArcFactor, 0.5f)
}
package com.dfd.delfin.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import com.dfd.delfin.R
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.names

/**
 * Custom implementation of [AppCompatAutoCompleteTextView]
 */
class DelfinCityAutoEditText(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatAutoCompleteTextView(context, attrs) {

  private val RightGap = context.resources.getDimension(R.dimen.size_12dp)
  private val DotsGap = context.resources.getDimension(R.dimen.size_6dp)
  private val DotRadius = context.resources.getDimension(R.dimen.size_2dp)
  private val MaxTransY = context.resources.getDimension(R.dimen.size_4dp)

    private var isSelectionInProgress = false

  private val paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
      alpha = 100
    }
  }

  private val errorPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.RED
      alpha = 100
    }
  }

  /* animation factor */
  private var factor = 0.0f

  private var progress = false
  private var error = false

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (progress) {
      canvas.apply {
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
    } else if (error && hasFocus()) {
      canvas?.apply {
        drawCircle(width - RightGap - DotRadius, height / 2f, DotRadius, errorPaint)
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

  fun setItems(
    cities: List<CityModel>,
    selected: (CityModel) -> Unit
  ) {
    if (!isPerformingCompletion) {
      progress(false)
      val adapter =
        ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, cities.names())
      setAdapter(adapter)
      setOnItemClickListener { _, _, i, _ ->
          isSelectionInProgress = true
          //
        setText(cities[i].cityName())
        selected(cities[i])
        dismissDropDown()

          // Reset flag after a short delay
          postDelayed({
              isSelectionInProgress = false
              // Clear after longer delay to prevent race conditions
          }, 500)
      }
    }
    if (cities.isEmpty()) {
      error = true
      dismissDropDown()
    } else {
      error = false
    }
    invalidate()
  }

  fun errorAnimate() {
    val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
    this.startAnimation(shake)
  }


    // Check if selection is in progress
    fun isSelectionInProgress(): Boolean {
        return isSelectionInProgress
    }
}

private const val DotsCount = 3
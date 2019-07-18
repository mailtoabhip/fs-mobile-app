package com.delhivery.axle.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.names

class DelhiveryCityAutoEditText(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatAutoCompleteTextView(context, attrs) {

  private val RightGap = context.resources.getDimension(R.dimen.size_12dp)
  private val DotsGap = context.resources.getDimension(R.dimen.size_6dp)
  private val DotRadius = context.resources.getDimension(R.dimen.size_2dp)
  private val MaxTransY = context.resources.getDimension(R.dimen.size_4dp)

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
    progress(false)
    val adapter =
      ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, cities.names())
    setAdapter(adapter)
    setOnItemClickListener { _, _, i, _ ->
      setText(cities[i].cityName())
      selected(cities[i])
      dismissDropDown()
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
}

private const val DotsCount = 3
package com.delhivery.axle.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.delhivery.axle.R

/**
 * ViewPagerIndicator for [ViewPager]
 */
class DelhiveryViewPagerIndicator(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {

  /* Inactive dot radius factor */
  private val InactiveDotRadiusFactor = 0.75f

  /* active dot radius */
  private val DotRadius by lazy {
    resources.getDimension(
        R.dimen.delhivery_view_pager_indicator_dot_radius
    )
  }
  /* dots gap */
  private val DotsGap by lazy {
    resources.getDimension(
        R.dimen.delhivery_view_pager_indicator_dots_gap
    )
  }

  /* Colors */
  private val InactiveDotColor = ContextCompat.getColor(context, R.color.viewpager_dot_inactive)
  private val ActiveDotColor = ContextCompat.getColor(context, R.color.viewpager_dot_active)

  private var totalWidth = 0f
  private var activeDotCx = 0f
  private var pageCount = 0

  /* view pager */
  var viewPager: ViewPager? = null
    set(value) {
      setupWithPager(value)
      invalidate()
    }

  private val inActivepaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = InactiveDotColor
    alpha = 110
  }

  private val activepaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = ActiveDotColor
  }

  override fun onDraw(canvas: Canvas) {
    if (pageCount <= 0) return

    canvas?.apply {
      var startCx = startCx()
      for (i in 0 until pageCount) {
        drawCircle(startCx, height / 2f, DotRadius * InactiveDotRadiusFactor, inActivepaint)
        startCx += ((2 * DotRadius) + DotsGap)
      }
      drawCircle(activeDotCx, height / 2f, DotRadius, activepaint)
    }
  }

  /**
   * Start cx of first dot
   */
  private fun startCx() = (width / 2f - totalWidth / 2f) + DotRadius

  private fun setupWithPager(viewPager: ViewPager?) {
    viewPager?.apply {
      pageCount = adapter?.count ?: 0
      /* calculate total width */
      totalWidth = (2 * DotRadius * pageCount) + (DotsGap * (pageCount - 1))

      addOnPageChangeListener(object : OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {

        }

        override fun onPageScrolled(
          position: Int,
          positionOffset: Float,
          positionOffsetPixels: Int
        ) {
          activeDotCx = startCx() + ((2 * DotRadius + DotsGap) * (position + positionOffset))
          this@DelhiveryViewPagerIndicator.invalidate()
        }

        override fun onPageSelected(p0: Int) {
        }
      })
    }
  }
}
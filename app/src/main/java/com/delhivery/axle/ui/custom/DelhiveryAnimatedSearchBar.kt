package com.delhivery.axle.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.RectF
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.delhivery.axle.R
import com.delhivery.axle.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.axle.utils.extensions.disposeAndClear
import com.delhivery.axle.utils.extensions.getBitmapRes
import com.delhivery.axle.utils.extensions.plusAssign
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable

/**
 * Custom implementation of [EditText]
 */
class DelhiveryAnimatedSearchBar(
  context: Context,
  attrs: AttributeSet? = null
) : EditText(context, attrs) {

  init {
    background = null
    maxLines = 1
  }

  private val searchIconPadding by lazy {
    resources.getDimension(R.dimen.size_20dp)
  }

  /* final padding */
  private val padding by lazy {
    resources.getDimension(R.dimen.size_8dp)
  }

  private val radius by lazy {
    resources.getDimension(R.dimen.size_6dp)
  }

  private val primaryBgPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
      style = FILL
      setShadowLayer(padding / 2f, 0f, 0f, Color.parseColor("#37000000"))
    }
  }

  private val secondaryBgPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
      style = FILL
    }
  }

  private val searchIcon by lazy {
    context.getBitmapRes(R.drawable.ic_search_black)
  }

  private val cancelIcon by lazy {
    context.getBitmapRes(R.drawable.ic_close_black)
  }

  private var animationRatio = 0f

  private var adapter: BaseFilterableDataRVAdapter<*, *, *>? = null
  private var compositeDisposable = CompositeDisposable()

  @SuppressLint("DrawAllocation")
  override fun onDraw(canvas: Canvas?) {
    canvas?.apply {
      val animatedPadding = padding * animationRatio
      val primaryBgRect = RectF(
          animatedPadding,
          animatedPadding,
          width - animatedPadding,
          height - animatedPadding - padding
      )
      val animatedRadius = radius * animationRatio
      drawRoundRect(primaryBgRect, animatedRadius, animatedRadius, primaryBgPaint)
      val secondaryBgRect = RectF(
          padding, padding, width - padding, height - 2 * padding
      )
      secondaryBgPaint.alpha = (255f * (1f - animationRatio)).toInt()
      drawRoundRect(secondaryBgRect, radius, radius, secondaryBgPaint)

      /* draw search icon */
      canvas.drawBitmap(
          searchIcon, padding + searchIconPadding, (height - padding - searchIcon.height) / 2f,
          Paint(Paint.ANTI_ALIAS_FLAG)
      )
    }
    super.onDraw(canvas)
  }

  fun setRatio(ratio: Float) {
    animationRatio = ratio
    invalidate()
  }

  override fun onKeyPreIme(
    keyCode: Int,
    event: KeyEvent?
  ) = run {
    if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
      clearFocus()
      adapter?.cancelFilter()
    }
    super.onKeyPreIme(keyCode, event)
  }

  /**
   * Attach search bar with adapter
   */
  fun attachWithAdapter(
    _adapter: BaseFilterableDataRVAdapter<*, *, *>,
    elevationChangeListener: ToolbarElevationChangeListener
  ) {
    if (adapter != null) throw IllegalStateException("Already attached to one adapter: $adapter")
    this.adapter = _adapter

    compositeDisposable += RxTextView.textChanges(this)
        .filter { it.isNotEmpty() }
        .map { it.toString() }
        .subscribe { this.adapter?.filter(it) }

    setOnFocusChangeListener { _, focused ->
      if (focused) {
        this.adapter?.enableFilter()
      } else {
        this.adapter?.cancelFilter()
        elevationChangeListener.postElevation(resources.getDimension(R.dimen.toolbar_elevation))
        setText("")
      }
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    compositeDisposable.disposeAndClear()
  }

  interface ToolbarElevationChangeListener {
    fun postElevation(elevation: Float)
  }
}
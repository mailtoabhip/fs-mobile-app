package com.delhivery.axle.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.RectF
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.delhivery.axle.R
import com.delhivery.axle.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.axle.utils.extensions.disposeAndClear
import com.delhivery.axle.utils.extensions.getBitmapRes
import com.delhivery.axle.utils.extensions.plusAssign
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable

/**
 * Custom implementation of [EditText] matching view_bids_searchbar_new_item design
 */
class DelhiveryBidAnimatedSearchBar(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

  init {
    background = null
    maxLines = 1
    hint = context.getString(R.string.action_search)
    textSize = 14f
    setTextColor(ResourcesCompat.getColor(resources, R.color.color_hint, null))
    setHintTextColor(ResourcesCompat.getColor(resources, R.color.color_hint, null))
    inputType = android.text.InputType.TYPE_CLASS_TEXT
  }

  private val searchIconPadding by lazy {
    resources.getDimension(R.dimen.size_8dp)
  }

  /* final padding */
  private val padding by lazy {
    resources.getDimension(R.dimen.size_12dp)
  }

  private val radius by lazy {
    resources.getDimension(R.dimen.size_6dp)
  }

  private val borderWidth by lazy {
    resources.getDimension(R.dimen.size_1dp)
  }

  private val whitePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      style = FILL
    }
  }

  private val borderPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#E0E3EB")
      style = STROKE
      strokeWidth = borderWidth
    }
  }

  private val searchIcon by lazy {
    context.getBitmapRes(R.drawable.ic_search)
  }

  private var animationRatio = 0f

  private var adapter: BaseFilterableDataRVAdapter<*, *, *>? = null
  private var compositeDisposable = CompositeDisposable()

  @SuppressLint("DrawAllocation")
  override fun onDraw(canvas: Canvas) {
    canvas.apply {
      // Draw white background with rounded corners
      val backgroundRect = RectF(
          padding,
          padding,
          width - padding,
          height - padding
      )
      drawRoundRect(backgroundRect, radius, radius, whitePaint)
      
      // Draw border
      drawRoundRect(backgroundRect, radius, radius, borderPaint)

      /* draw search icon */
      val iconLeft = padding + searchIconPadding
      val iconTop = (height - searchIcon.height) / 2f
      canvas.drawBitmap(
          searchIcon, iconLeft, iconTop,
          Paint(Paint.ANTI_ALIAS_FLAG)
      )
    }
    
    // Adjust text padding to account for search icon
    val iconWidth = searchIcon.width
    val iconPadding = searchIconPadding
    val totalLeftPadding = padding + iconWidth + iconPadding + searchIconPadding
    
    setPadding(
        totalLeftPadding.toInt(),
        paddingTop,
        paddingRight,
        paddingBottom
    )
    
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
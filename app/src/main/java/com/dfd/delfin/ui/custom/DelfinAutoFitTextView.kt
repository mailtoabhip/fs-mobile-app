package com.dfd.delfin.ui.custom

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

/**
 * A [TextView] that re-sizes its text to be no larger than the width of the view.
 *
 * @attr ref R.styleable.AutofitTextView_sizeToFit
 * @attr ref R.styleable.AutofitTextView_minTextSize
 * @attr ref R.styleable.AutofitTextView_precision
 */
class DelfinAutoFitTextView : AppCompatTextView, DelfinAutofitHelper.OnTextSizeChangeListener {

  /**
   * Returns the [DelfinAutofitHelper] for this View.
   */
  var delfinAutofitHelper: DelfinAutofitHelper? = null
    private set

  /**
   * Returns whether or not the text will be automatically re-sized to fit its constraints.
   */
  /**
   * If true, the text will automatically be re-sized to fit its constraints; if false, it will
   * act like a normal TextView.
   */
  var isSizeToFit: Boolean
    get() = delfinAutofitHelper!!.isEnabled()
    set(sizeToFit) {
      delfinAutofitHelper!!.setEnabled(sizeToFit)
    }

  /**
   * Returns the maximum size (in pixels) of the text in this View.
   */
  /**
   * Set the maximum text size to the given value, interpreted as "scaled pixel" units. This size
   * is adjusted based on the current density and user font size preference.
   *
   * @param size The scaled pixel size.
   * @attr ref android.R.styleable#TextView_textSize
   */
  var maxTextSize: Float
    get() = delfinAutofitHelper!!.getMaxTextSize()
    set(size) {
      delfinAutofitHelper!!.setMaxTextSize(size)
    }

  /**
   * Returns the minimum size (in pixels) of the text in this View.
   */
  val minTextSize: Float
    get() = delfinAutofitHelper!!.getMinTextSize()

  /**
   * Returns the amount of precision used to calculate the correct text size to fit within its
   * bounds.
   */
  /**
   * Set the amount of precision used to calculate the correct text size to fit within its
   * bounds. Lower precision is more precise and takes more time.
   *
   * @param precision The amount of precision.
   */
  var precision: Float
    get() = delfinAutofitHelper!!.getPrecision()
    set(precision) {
      delfinAutofitHelper!!.setPrecision(precision)
    }

  constructor(context: Context) : super(context) {
    init(context, null, 0)
  }

  constructor(
    context: Context,
    attrs: AttributeSet
  ) : super(context, attrs) {
    init(context, attrs, 0)
  }

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    init(context, attrs, defStyle)
  }

  private fun init(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
  ) {
    delfinAutofitHelper = DelfinAutofitHelper.create(this, attrs, defStyle)
        .addOnTextSizeChangeListener(this)
  }

  // Getters and Setters

  /**
   * {@inheritDoc}
   */
  override fun setTextSize(
    unit: Int,
    size: Float
  ) {
    super.setTextSize(unit, size)
    if (delfinAutofitHelper != null) {
      delfinAutofitHelper!!.setTextSize(unit, size)
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun setLines(lines: Int) {
    super.setLines(lines)
    if (delfinAutofitHelper != null) {
      delfinAutofitHelper!!.setMaxLines(lines)
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun setMaxLines(maxLines: Int) {
    super.setMaxLines(maxLines)
    if (delfinAutofitHelper != null) {
      delfinAutofitHelper!!.setMaxLines(maxLines)
    }
  }

  /**
   * Sets the property of this field (sizeToFit), to automatically resize the text to fit its
   * constraints.
   */
  fun setSizeToFit() {
    isSizeToFit = true
  }

  /**
   * Set the maximum text size to a given unit and value. See TypedValue for the possible
   * dimension units.
   *
   * @param unit The desired dimension unit.
   * @param size The desired size in the given units.
   * @attr ref android.R.styleable#TextView_textSize
   */
  fun setMaxTextSize(
    unit: Int,
    size: Float
  ) {
    delfinAutofitHelper!!.setMaxTextSize(unit, size)
  }

  /**
   * Set the minimum text size to the given value, interpreted as "scaled pixel" units. This size
   * is adjusted based on the current density and user font size preference.
   *
   * @param minSize The scaled pixel size.
   * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
   */
  fun setMinTextSize(minSize: Int) {
    delfinAutofitHelper!!.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize.toFloat())
  }

  /**
   * Set the minimum text size to a given unit and value. See TypedValue for the possible
   * dimension units.
   *
   * @param unit The desired dimension unit.
   * @param minSize The desired size in the given units.
   * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
   */
  fun setMinTextSize(
    unit: Int,
    minSize: Float
  ) {
    delfinAutofitHelper!!.setMinTextSize(unit, minSize)
  }

  override fun onTextSizeChange(
    textSize: Float,
    oldTextSize: Float
  ) {
    // do nothing
  }
}
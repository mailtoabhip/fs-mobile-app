package com.delhivery.axle.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.delhivery.axle.R
import com.google.android.material.textfield.TextInputLayout

class DelhiveryTextInputLayout(
  context: Context,
  attrs: AttributeSet
) : TextInputLayout(context, attrs), TextWatcher {

  private var minLength: Int = Int.MIN_VALUE
  private var maxVal: Int = Int.MAX_VALUE

  init {
    attrs.let { a ->
      val typedArray = context.obtainStyledAttributes(a, R.styleable.DelhiveryTextInputLayout, 0, 0)
      minLength =
        typedArray.getInteger(R.styleable.DelhiveryTextInputLayout_min_length, Int.MIN_VALUE)
      maxVal = typedArray.getInt(R.styleable.DelhiveryTextInputLayout_max_val, Int.MAX_VALUE)
      typedArray.recycle()
    }
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    editText?.addTextChangedListener(this)
  }

  override fun afterTextChanged(p0: Editable?) {

  }

  override fun beforeTextChanged(
    p0: CharSequence?,
    p1: Int,
    p2: Int,
    p3: Int
  ) {
  }

  override fun onTextChanged(
    p0: CharSequence?,
    p1: Int,
    p2: Int,
    p3: Int
  ) {
    if (p0 != null) {
      val seq = p0.trim()
      if (seq.isEmpty()) {
        error = "*Please enter a value"
      } else if (minLength != Int.MIN_VALUE) {
        if (seq.length < minLength) {
          error = "*Min length required is $minLength"
        } else {
          error = null
        }
      } else if (maxVal != Int.MAX_VALUE && seq.isNotEmpty()) {
        val maxVal: Int
        try {
          maxVal = seq.toString()
              .toInt()
        } catch (e: Exception) {
          error = "*Invalid value"
          return
        }
        if (maxVal > this.maxVal) {
          error = "*Max value can be ${this.maxVal}"
        } else {
          error = null
        }
      } else if (seq.isNotEmpty()) {
        error = null
      }
    }

  }

  override fun setError(errorText: CharSequence?) {
    super.setError(errorText)

    try {
      val errorTextInput = findViewById<TextView>(R.id.textinput_error)
      val layoutParams =
        FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
      layoutParams.topMargin = 8
      errorTextInput.layoutParams = layoutParams
      if (errorText.isNullOrEmpty()) {
        errorTextInput.visibility = View.GONE
      } else {
        errorTextInput.visibility = View.VISIBLE
      }
    } catch (e: Exception) {
    }
  }

}
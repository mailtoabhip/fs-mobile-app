package com.delhivery.orion.ui.custom

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.delhivery.orion.R
import com.delhivery.orion.utils.extensions.raisedFocus

class DelhiveryOTPViewEditText(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
  init {
    /* get attributes */
    attrs?.let { a ->
      val typedArray = context.obtainStyledAttributes(a, R.styleable.DelhiveryOTPViewEditText, 0, 0)
      error = typedArray.getBoolean(R.styleable.DelhiveryOTPViewEditText_state_error, false)
      typedArray.recycle()
    }

    /* elevate on focus */
    raisedFocus()
  }

  /* error state */
  var error = false
    set(e) {
      setBackgroundResource(
          if (e) R.drawable.bg_login_edit_error else R.drawable.bg_login_edit
      )
    }

  /**
   * Add Length action as Text Watcher
   */
  fun lengthAction(
    length: Int,
    action: () -> Unit
  ) {
    addTextChangedListener(LengthTextWatcher(length, action))
  }

  /**
   * Length text watcher action on length
   */
  inner class LengthTextWatcher(
    private val length: Int,
    private val action: () -> Unit
  ) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
      if (s?.length ?: 0 == length) {
        action()
      }
    }

    override fun beforeTextChanged(
      s: CharSequence?,
      start: Int,
      count: Int,
      after: Int
    ) {
    }

    override fun onTextChanged(
      s: CharSequence?,
      start: Int,
      before: Int,
      count: Int
    ) {
    }
  }
}
package com.delhivery.axle.utils.extensions

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.annotation.ArrayRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.delhivery.axle.R
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

/**
 * Edit Text Clear text and request focus
 */
fun EditText.clearAndFocus() {
  setText("")
  if (requestFocus() && context is Activity) {
    (context as Activity).window.setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
    )
  }
}

/**
 * Applied to [EditText] get text and emit if not null and not empty
 * - flat map with chain
 *
 * @return Observable<String>
 */
fun EditText.rxText(): Observable<String> = Observable.just(text.toString().trim())
    .filter { it.isNotNullOrEmpty() }

/**
 * Check for [EditorInfo.IME_ACTION_DONE] editor action and pass text
 *
 * @return Observable<String> with [EditText] text
 */
fun EditText.textImeDone(): Observable<String> =
  RxTextView.editorActions(this)
      .filter { it == EditorInfo.IME_ACTION_DONE }
      .flatMap { rxText() }

/**
 * Show raised on focus of edit text
 */
fun EditText.raisedFocus() = setOnFocusChangeListener { v, hasFocus ->
  if (hasFocus) {
    v.context.resources.getDimension(R.dimen.edit_text_raise_focus_z)
  } else {
    0f
  }.let { z -> ViewCompat.setTranslationZ(v, z) }
}


/**
 * Action done
 */
fun EditText.actionDone(
  actionId: Int = EditorInfo.IME_ACTION_DONE,
  action: () -> Unit
) {
  setOnEditorActionListener { v, _actionId, event ->
    if (_actionId == actionId) {
      action()
      true
    } else {
      false
    }
  }
}

/**
 * Attach page selected to view pager
 */
fun ViewPager.onPageSelected(action: (pos: Int) -> Unit) = apply {
  addOnPageChangeListener(object : OnPageChangeListener {
    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(
      p0: Int,
      p1: Float,
      p2: Int
    ) {
    }

    override fun onPageSelected(p0: Int) {
      action(p0)
    }
  })
}

/**
 * Setup simple array adapter
 */
fun Spinner.setup(@ArrayRes resId: Int, selected: (pos: Int, value: String?) -> Unit) {
  ArrayAdapter.createFromResource(this.context, resId, android.R.layout.simple_spinner_item)
      .also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        onItemSelectedListener = object : OnItemSelectedListener {
          override fun onNothingSelected(p0: AdapterView<*>?) {
            selected(-1, null)
          }
          override fun onItemSelected(
            p0: AdapterView<*>?,
            p1: View?,
            p2: Int,
            p3: Long
          ) {
            selected(p2, getItemAtPosition(p2).toString())
          }
        }
        adapter = it
      }
}

fun Spinner.setHintColor(selectedText: String?){
  if(selectedText!!.toLowerCase().contains("select") && selectedView!=null)
    (selectedView as TextView).setTextColor(Color.GRAY)
}
/**
 * Set view visibility
 */
fun View.visible(visible: Boolean) = apply {
  visibility = when (visible) {
    true -> View.VISIBLE
    false -> View.GONE
  }
}

/**
 * View center X
 */
fun View.centerX() = left + (width / 2f)

/**
 * View center Y
 */
fun View.centerY() = top + (height / 2f)

fun View.focusClick() = apply {
  requestFocus()
  post { performClick() }
}

/**
 * Consume view touch events
 */
fun View.consumeTouch() = setOnTouchListener { _, _ -> true }

/**
 * Change view visibility as per mutable live data
 */
fun View.withMutableData(
  owner: LifecycleOwner,
  data: MutableLiveData<Boolean>
) = data.observe(owner, Observer {
  when (this) {
    is FloatingActionButton -> if (it == true) show() else hide()
    else -> visible(it == true)
  }
})

inline var TextView.underline: Boolean
  set(visible) {
    paintFlags = if (visible)
      paintFlags or Paint.UNDERLINE_TEXT_FLAG
    else paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
  }
  get() = paintFlags and Paint.UNDERLINE_TEXT_FLAG == Paint.UNDERLINE_TEXT_FLAG
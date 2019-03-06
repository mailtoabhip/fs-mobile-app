package com.delhivery.orion.utils.extensions

import android.app.Activity
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.delhivery.orion.R
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
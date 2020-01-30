package com.delhivery.axle.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Custom implementation of viewpager to enable/disable scroll
 *
 **
 */
class DelhiverViewPager(
  context: Context,
  attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

  private var disable = false

  override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    return !disable && super.onInterceptTouchEvent(event)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return !disable && super.onTouchEvent(event)
  }

  /**
   * When disable = true not work the scroll
   * when disable = false work the scroll
   */
  fun disableScroll(disable: Boolean) {
    this.disable = disable
  }

}
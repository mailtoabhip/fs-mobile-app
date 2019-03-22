package com.delhivery.orion.utils.extensions

import android.support.v7.widget.LinearLayoutCompat
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout

/**
 * Calculate number of children that can be accomodated in [LinearLayout]
 */
fun LinearLayoutCompat.calculateChildren(childView: View): Int {
  /* measure child */
  childView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
  /* orientation based calculation */
  return when (orientation) {
    LinearLayout.VERTICAL -> {
      val childHeight = childView.measuredHeight
      ((height - childHeight / 2f) / childHeight).toInt()
    }
    LinearLayout.HORIZONTAL -> 0 //todo-implement for horizontal ll when needed
    else -> 0
  }
}
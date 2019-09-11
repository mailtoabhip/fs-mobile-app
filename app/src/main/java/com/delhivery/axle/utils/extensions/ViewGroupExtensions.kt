package com.delhivery.axle.utils.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.widget.LinearLayoutCompat
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

/**
 * Bind [SwipeRefreshLayout] with progress live data with boolean
 */
fun SwipeRefreshLayout.progressLiveData(
  liveData: MutableLiveData<Boolean>,
  owner: LifecycleOwner
) = liveData.observe(owner, Observer {
  it?.let { show ->
    if (!show) {
      isRefreshing = false
    } else if (!isRefreshing) {
      isRefreshing = true
    }
  }
})
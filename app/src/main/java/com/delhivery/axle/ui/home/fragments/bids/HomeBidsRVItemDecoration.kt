package com.delhivery.axle.ui.home.fragments.bids

import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.databinding.ViewHomeBidsStickySearchItemBinding

/**
 * Useless for now, **DO NOT DELETE** calculations will be required
 */
class HomeBidsRVItemDecoration : ItemDecoration() {

  private val HeaderOffset = 0
  private var headerBinding: ViewHomeBidsStickySearchItemBinding? = null

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: androidx.recyclerview.widget.RecyclerView,
    state: State
  ) {
    super.getItemOffsets(outRect, view, parent, state)

    val pos = parent.getChildAdapterPosition(view)
    if (pos == 1) {
//      outRect.top = HeaderOffset
    }
  }

  override fun onDrawOver(
    c: Canvas,
    parent: androidx.recyclerview.widget.RecyclerView,
    state: State
  ) {
    super.onDrawOver(c, parent, state)

    if (headerBinding == null) {
      initBinding(parent)
      fixLayoutSize(parent)
    }

    for (i in 0 until parent.childCount) {
      val view = parent.getChildAt(i)
      val position = parent.getChildAdapterPosition(view)
      if (position == 1) {
        drawHeader(c, view)
      }
    }
  }

  private fun drawHeader(
    canvas: Canvas,
    child: View
  ) {
    val headerTop = child.top + (child.height - headerBinding!!.root.height) * 1f
    Log.d("harish", "headerTop:$headerTop")
//    if (headerTop < 0) {
    canvas.save()
    canvas.translate(0f, 0f)
    headerBinding!!.root.draw(canvas)
    canvas.restore()
//    }
  }

  private fun initBinding(parent: androidx.recyclerview.widget.RecyclerView) {
    headerBinding = ViewHomeBidsStickySearchItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
    )
  }

  private fun fixLayoutSize(parent: androidx.recyclerview.widget.RecyclerView) {
    val headerView = headerBinding!!.root
    val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

    val childWidth = ViewGroup.getChildMeasureSpec(
        widthSpec,
        parent.paddingLeft + parent.paddingRight,
        headerView.layoutParams.width
    )

    val childHeight = ViewGroup.getChildMeasureSpec(
        heightSpec,
        parent.paddingTop + parent.paddingBottom,
        headerView.layoutParams.height
    )

    headerView.measure(childWidth, childHeight)
    headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)
  }
}
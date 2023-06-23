package com.delhivery.axle.ui.selectroute.fragments.routeslist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.ItemTouchHelper
import com.delhivery.axle.R
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * <Define what the class does>
 *
 **
 */
class SwipeToDeleteCallback @Inject constructor(
  private val context: Context?,
  private val _adapter: RoutesRVAdapter
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

  private val adapter: RoutesRVAdapter

  private val icon: Drawable
  private val background: ColorDrawable

  init {
    adapter = _adapter
    icon = ContextCompat.getDrawable(
        context!!, R.drawable.ic_add_black_24dp
    )!!
    background = ColorDrawable(Color.RED)
  }

  override fun onMove(
    p0: androidx.recyclerview.widget.RecyclerView,
    p1: ViewHolder,
    p2: ViewHolder
  ): Boolean {
    return false
  }

  override fun onSwiped(
    p0: ViewHolder,
    p1: Int
  ) {
    val position = p0.bindingAdapterPosition
    _adapter.deleteItem(position)
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: androidx.recyclerview.widget.RecyclerView,
    viewHolder: ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    val itemView = viewHolder.itemView
    val backgroundCornerOffset = 20

    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
    val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
    val iconBottom = iconTop + icon.intrinsicHeight

    if (dX > 0) { // Swiping to the right
      val iconLeft = itemView.left + iconMargin + icon.intrinsicHeight
      val iconRight = itemView.left + iconMargin
      icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

      background.setBounds(
          itemView.left, itemView.top,
          itemView.left + dX.toInt() + backgroundCornerOffset,
          itemView.bottom
      )
    } else if (dX < 0) { // Swiping to the left
      val iconLeft = itemView.right - iconMargin - icon.intrinsicHeight
      val iconRight = itemView.right - iconMargin
      icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

      background.setBounds(
          itemView.right + dX.toInt() - backgroundCornerOffset,
          itemView.top, itemView.right, itemView.bottom
      )
    } else { // view is unSwiped
      background.setBounds(0, 0, 0, 0)
    }

    background.draw(c)
    icon.draw(c)

  }

}
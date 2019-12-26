package com.delhivery.axle.utils.expandablerecyclerview.viewholders

import android.view.View
import android.view.View.OnClickListener
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.utils.expandablerecyclerview.listeners.OnGroupClickListener
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup

/**
 * ViewHolder for the [#title][ExpandableGroup] in a [ExpandableGroup]
 *
 * The current implementation does now allow for sub [View] of the parent view to trigger
 * a collapse / expand. *Only* click events on the parent [View] will trigger a collapse or
 * expand
 */
abstract class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    OnClickListener {

  private var listener: OnGroupClickListener? = null

  init {
    itemView.setOnClickListener(this)
  }

  override fun onClick(v: View) {
    if (listener != null) {
      listener!!.onGroupClick(adapterPosition)
    }
  }

  fun setOnGroupClickListener(listener: OnGroupClickListener) {
    this.listener = listener
  }

  fun expand() {}

  fun collapse() {}
}

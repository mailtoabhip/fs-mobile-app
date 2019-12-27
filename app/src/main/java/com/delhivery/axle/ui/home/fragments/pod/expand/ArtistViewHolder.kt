package com.delhivery.axle.ui.home.fragments.pod.expand

import android.view.View
import android.widget.TextView
import com.delhivery.axle.utils.expandablerecyclerview.viewholders.ChildViewHolder

class ArtistViewHolder(itemView: View) : ChildViewHolder(itemView) {

  private val childTextView: TextView? = null

  init {
//    childTextView = itemView.findViewById<View>(R.id.list_item_artist_name) as TextView
  }

  fun setArtistName(name: String) {
    childTextView?.text = name
  }
}

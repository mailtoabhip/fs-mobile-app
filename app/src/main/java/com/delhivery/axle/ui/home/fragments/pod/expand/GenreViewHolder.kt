package com.delhivery.axle.ui.home.fragments.pod.expand

import android.view.View
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.delhivery.axle.ui.home.fragments.pod.Genre
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup
import com.delhivery.axle.utils.expandablerecyclerview.viewholders.GroupViewHolder

class GenreViewHolder(itemView: View) : GroupViewHolder(itemView) {

  lateinit var genreName: TextView
  lateinit var arrow: ImageView
  lateinit var icon: ImageView

  init {
//    genreName = itemView.findViewById<View>(R.id.list_item_genre_name) as TextView
//    arrow = itemView.findViewById<View>(R.id.list_item_genre_arrow) as ImageView
//    icon = itemView.findViewById<View>(R.id.list_item_genre_icon) as ImageView
  }

  fun setGenreTitle(genre: ExpandableGroup<*>) {
    if (genre is Genre) {
      genreName.text = genre.title
      icon.setBackgroundResource(genre.iconResId)
    }
  }

  override fun expand() {
    animateExpand()
  }

  override fun collapse() {
    animateCollapse()
  }

  private fun animateExpand() {
    val rotate = RotateAnimation(360f, 180f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
    rotate.duration = 300
    rotate.fillAfter = true
    arrow.animation = rotate
  }

  private fun animateCollapse() {
    val rotate = RotateAnimation(180f, 360f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
    rotate.duration = 300
    rotate.fillAfter = true
    arrow.animation = rotate
  }
}

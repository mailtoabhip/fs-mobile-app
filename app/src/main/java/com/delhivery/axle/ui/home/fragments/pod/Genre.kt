package com.delhivery.axle.ui.home.fragments.pod

import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup

class Genre(
  title: String,
  items: List<Artist>,
  val iconResId: Int
) : ExpandableGroup<Artist>(title, items) {

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o !is Genre) return false

    val genre = o as Genre?

    return iconResId == genre!!.iconResId
  }

  override fun hashCode(): Int {
    return iconResId
  }
}


package com.delhivery.axle.ui.home.fragments.pod.expand

import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.fragments.pod.Genre
import com.delhivery.axle.utils.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup

class GenreAdapter(groups: List<ExpandableGroup<*>>) : ExpandableRecyclerViewAdapter<GenreViewHolder, ArtistViewHolder>(
    groups
) {

  override fun onCreateGroupViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): GenreViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.view_pod_parent_item, parent, false)
    return GenreViewHolder(view)
  }

  override fun onCreateChildViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ArtistViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.view_pod_child_item, parent, false)
    return ArtistViewHolder(view)
  }

  override fun onBindChildViewHolder(
    holder: ArtistViewHolder,
    flatPosition: Int,
    group: ExpandableGroup<*>,
    childIndex: Int
  ) {
    val artist = (group as Genre).items!![childIndex]
    holder.setArtistName(artist.name ?: "")
  }

  override fun onBindGroupViewHolder(
    holder: GenreViewHolder,
    flatPosition: Int,
    group: ExpandableGroup<*>
  ) {
    holder.setGenreTitle(group)
  }
}

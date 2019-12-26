package com.delhivery.axle.utils.expandablerecyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableList
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableListPosition
import com.delhivery.axle.utils.expandablerecyclerview.viewholders.ChildViewHolder
import com.delhivery.axle.utils.expandablerecyclerview.viewholders.GroupViewHolder

abstract class MultiTypeExpandableRecyclerViewAdapter<GVH : GroupViewHolder, CVH : ChildViewHolder>(groups: List<ExpandableGroup<*>>) :
    ExpandableRecyclerViewAdapter<GVH, CVH>(groups) {

  /**
   * Implementation of RecyclerView.Adapter.onCreateViewHolder(ViewGroup, int)
   * that determines if the list item is a group or a child and calls through
   * to the appropriate implementation of either [.onCreateGroupViewHolder]
   * or [.onCreateChildViewHolder]}.
   *
   * @param parent The [ViewGroup] into which the new [android.view.View]
   * will be added after it is bound to an adapter position.
   * @param viewType The view type of the new `android.view.View`.
   * @return Either a new [GroupViewHolder] or a new [ChildViewHolder]
   * that holds a `android.view.View` of the given view type.
   */
  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    if (isGroup(viewType)) {
      val gvh = onCreateGroupViewHolder(parent, viewType)
      gvh.setOnGroupClickListener(this)
      return gvh
    } else if (isChild(viewType)) {
      return onCreateChildViewHolder(parent, viewType)
    }
    throw IllegalArgumentException("viewType is not valid")
  }

  /**
   * Implementation of Adapter.onBindViewHolder(RecyclerView.ViewHolder, int)
   * that determines if the list item is a group or a child and calls through
   * to the appropriate implementation of either [.onBindGroupViewHolder]
   * or [.onBindChildViewHolder].
   *
   * @param holder Either the GroupViewHolder or the ChildViewHolder to bind data to
   * @param position The flat position (or index in the list of [ ][ExpandableList.getVisibleItemCount] in the list at which to bind
   */
  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    val listPos = expandableList.getUnflattenedPosition(position)
    val group = expandableList.getExpandableGroup(listPos)
    if (isGroup(getItemViewType(position))) {
      onBindGroupViewHolder(holder as GVH, position, group)

      if (isGroupExpanded(group)) {
        holder.expand()
      } else {
        holder.collapse()
      }
    } else if (isChild(getItemViewType(position))) {
      onBindChildViewHolder(holder as CVH, position, group, listPos.childPos)
    }
  }

  /**
   * Gets the view type of the item at the given position.
   *
   * @param position The flat position in the list to get the view type of
   * @return if the flat position corresponds to a child item, this will return the value returned
   * by `getChildViewType`. if the flat position refers to a group item this will return the
   * value returned by `getGroupViewType`
   */
  override fun getItemViewType(position: Int): Int {
    val listPosition = expandableList.getUnflattenedPosition(position)
    val group = expandableList.getExpandableGroup(listPosition)

    val viewType = listPosition.type
    when (viewType) {
      ExpandableListPosition.GROUP -> return getGroupViewType(position, group)
      ExpandableListPosition.CHILD -> return getChildViewType(
          position, group, listPosition.childPos
      )
      else -> return viewType
    }
  }

  /**
   * Used to allow subclasses to have multiple view types for children
   *
   * @param position the flat position in the list
   * @param group the group that this child belongs to
   * @param childIndex the index of the child within the group
   * @return any int representing the viewType for a child within the `group` *EXCEPT*
   * for [ExpandableListPosition.CHILD] and [ExpandableListPosition.GROUP].
   *
   * If you do *not* override this method, the default viewType for a group is [ ][ExpandableListPosition.CHILD]
   *
   *
   *
   * A subclass may use any number *EXCEPT* for [ExpandableListPosition.CHILD] and [ ][ExpandableListPosition.GROUP] as those are already being used by the adapter
   *
   */
  fun getChildViewType(
    position: Int,
    group: ExpandableGroup<*>,
    childIndex: Int
  ): Int {
    return super.getItemViewType(position)
  }

  /**
   * Used to allow subclasses to have multiple view types for groups
   *
   * @param position the flat position in the list
   * @param group the group at this position
   * @return any int representing the viewType for this `group` *EXCEPT*
   * for [ExpandableListPosition.CHILD] and [ExpandableListPosition.GROUP].
   *
   * If you do not override this method, the default viewType for a group is [ ][ExpandableListPosition.GROUP]
   *
   *
   *
   * A subclass may use any number *EXCEPT* for [ExpandableListPosition.CHILD] and [ ][ExpandableListPosition.GROUP] as those are already being used by the adapter
   *
   */
  fun getGroupViewType(
    position: Int,
    group: ExpandableGroup<*>
  ): Int {
    return super.getItemViewType(position)
  }

  /**
   * @param viewType the int corresponding to the viewType of a `ExpandableGroup`
   * @return if a subclasses has *NOT* overridden `getGroupViewType` than the viewType for
   * the group is defaulted to [ExpandableListPosition.GROUP]
   */
  fun isGroup(viewType: Int): Boolean {
    return viewType == ExpandableListPosition.GROUP
  }

  /**
   * @param viewType the int corresponding to the viewType of a child of a `ExpandableGroup`
   * @return if a subclasses has *NOT* overridden `getChildViewType` than the viewType for
   * the child is defaulted to [ExpandableListPosition.CHILD]
   */
  fun isChild(viewType: Int): Boolean {
    return viewType == ExpandableListPosition.CHILD
  }
}

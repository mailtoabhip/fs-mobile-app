package com.delhivery.axle.utils.expandablerecyclerview.listeners

import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup

interface ExpandCollapseListener {

  /**
   * Called when a group is expanded
   *
   * @param actionId
   * @param positionStart the flat position of the first child in the [ExpandableGroup]
   * @param itemCount the total number of children in the [ExpandableGroup]
   */
  fun onGroupExpanded(
    actionId: String,
    positionStart: Int,
    itemCount: Int
  )

  /**
   * Called when a group is collapsed
   *
   * @param actionId
   * @param positionStart the flat position of the first child in the [ExpandableGroup]
   * @param itemCount the total number of children in the [ExpandableGroup]
   */
  fun onGroupCollapsed(
    actionId: String,
    positionStart: Int,
    itemCount: Int
  )
}

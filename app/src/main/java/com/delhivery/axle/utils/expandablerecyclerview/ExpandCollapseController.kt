package com.delhivery.axle.utils.expandablerecyclerview

import com.delhivery.axle.utils.expandablerecyclerview.listeners.ExpandCollapseListener
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableGroup
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableList
import com.delhivery.axle.utils.expandablerecyclerview.models.ExpandableListPosition

/**
 * This class is the sits between the backing [ExpandableList] and the [ ] and mediates the expanding and collapsing of [ ]
 */
class ExpandCollapseController(
  private val expandableList: ExpandableList,
  private val listener: ExpandCollapseListener?
) {

  /**
   * Collapse a group
   *
   * @param listPosition position of the group to collapse
   */
  private fun collapseGroup(listPosition: ExpandableListPosition) {
    expandableList.expandedGroupIndexes[listPosition.groupPos] = false
    listener?.onGroupCollapsed(
        expandableList.getFlattenedGroupIndex(listPosition) + 1,
        expandableList.groups[listPosition.groupPos].itemCount
    )
  }

  /**
   * Expand a group
   *
   * @param listPosition the group to be expanded
   */
  private fun expandGroup(listPosition: ExpandableListPosition) {
    expandableList.expandedGroupIndexes[listPosition.groupPos] = true
    listener?.onGroupExpanded(
        expandableList.getFlattenedGroupIndex(listPosition) + 1,
        expandableList.groups[listPosition.groupPos].itemCount
    )
  }

  /**
   * @param group the [ExpandableGroup] being checked for its collapsed state
   * @return true if `group` is expanded, false if it is collapsed
   */
  fun isGroupExpanded(group: ExpandableGroup<*>): Boolean {
    val groupIndex = expandableList.groups.indexOf(group)
    return expandableList.expandedGroupIndexes[groupIndex]
  }

  /**
   * @param flatPos the flattened position of an item in the list
   * @return true if `group` is expanded, false if it is collapsed
   */
  fun isGroupExpanded(flatPos: Int): Boolean {
    val listPosition = expandableList.getUnflattenedPosition(flatPos)
    return expandableList.expandedGroupIndexes[listPosition.groupPos]
  }

  /**
   * @param flatPos The flat list position of the group
   * @return false if the group is expanded, *after* the toggle, true if the group is now collapsed
   */
  fun toggleGroup(flatPos: Int): Boolean {
    val listPos = expandableList.getUnflattenedPosition(flatPos)
    val expanded = expandableList.expandedGroupIndexes[listPos.groupPos]
    if (expanded) {
      collapseGroup(listPos)
    } else {
      expandGroup(listPos)
    }
    return expanded
  }

  fun toggleGroup(group: ExpandableGroup<*>): Boolean {
    val listPos =
      expandableList.getUnflattenedPosition(expandableList.getFlattenedGroupIndex(group))
    val expanded = expandableList.expandedGroupIndexes[listPos.groupPos]
    if (expanded) {
      collapseGroup(listPos)
    } else {
      expandGroup(listPos)
    }
    return expanded
  }
}
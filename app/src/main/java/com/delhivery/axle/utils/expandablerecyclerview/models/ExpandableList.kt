package com.delhivery.axle.utils.expandablerecyclerview.models

/*
 * Terminology:
 * <li> flat position - Flat list position, the position of an item relative to all the
 * other *visible* items on the screen. For example, if you have a three groups, each with
 * 2 children and all are collapsed, the "flat position" of the last group would be 2. And if
 * the first of those three groups was expanded, the flat position of the last group would now be 4.
 *
 *
 * This class acts as a translator between the flat list position - i.e. what groups
 * and children you see on the screen - to and from the full backing list of groups & their children
 */
class ExpandableList(var groups: List<ExpandableGroup<*>>) {
  var expandedGroupIndexes: BooleanArray = BooleanArray(groups.size)

  /**
   * @return the total number visible rows
   */
  val visibleItemCount: Int
    get() {
      var count = 0
      for (i in groups.indices) {
        count += numberOfVisibleItemsInGroup(i)
      }
      return count
    }

  init {

    for (i in groups.indices) {
      expandedGroupIndexes[i] = false
    }
  }

  /**
   * @param group the index of the [ExpandableGroup] in the full collection [.groups]
   * @return the number of visible row items for the particular group. If the group is collapsed,
   * return 1 for the group header. If the group is expanded return total number of children in the
   * group + 1 for the group header
   */
  private fun numberOfVisibleItemsInGroup(group: Int): Int {
    return if (expandedGroupIndexes[group]) {
      groups[group].itemCount + 1
    } else {
      1
    }
  }

  /**
   * Translates a flat list position (the raw position of an item (child or group) in the list) to
   * either a) group pos if the specified flat list position corresponds to a group, or b) child
   * pos if it corresponds to a child.  Performs a binary search on the expanded
   * groups list to find the flat list pos if it is an exp group, otherwise
   * finds where the flat list pos fits in between the exp groups.
   *
   * @param flPos the flat list position to be translated
   * @return the group position or child position of the specified flat list
   * position encompassed in a [ExpandableListPosition] object
   * that contains additional useful info for insertion, etc.
   */
  fun getUnflattenedPosition(flPos: Int): ExpandableListPosition {
    var groupItemCount: Int
    var adapted = flPos
    for (i in groups.indices) {
      groupItemCount = numberOfVisibleItemsInGroup(i)
      if (adapted == 0) {
        return ExpandableListPosition.obtain(ExpandableListPosition.GROUP, i, -1, flPos)
      } else if (adapted < groupItemCount) {
        return ExpandableListPosition.obtain(ExpandableListPosition.CHILD, i, adapted - 1, flPos)
      }
      adapted -= groupItemCount
    }
    throw RuntimeException("Unknown state")
  }

  /**
   * @param listPosition representing either a child or a group
   * @return the index of a group within the [.getVisibleItemCount]
   */
  fun getFlattenedGroupIndex(listPosition: ExpandableListPosition): Int {
    val groupIndex = listPosition.groupPos
    var runningTotal = 0

    for (i in 0 until groupIndex) {
      runningTotal += numberOfVisibleItemsInGroup(i)
    }
    return runningTotal
  }

  /**
   * @param groupIndex representing the index of a group within [.groups]
   * @return the index of a group within the [.getVisibleItemCount]
   */
  fun getFlattenedGroupIndex(groupIndex: Int): Int {
    var runningTotal = 0

    for (i in 0 until groupIndex) {
      runningTotal += numberOfVisibleItemsInGroup(i)
    }
    return runningTotal
  }

  /**
   * @param group an [ExpandableGroup] within [.groups]
   * @return the index of a group within the [.getVisibleItemCount] or 0 if the
   * groups.indexOf cannot find the group
   */
  fun getFlattenedGroupIndex(group: ExpandableGroup<*>): Int {
    val groupIndex = groups.indexOf(group)
    var runningTotal = 0

    for (i in 0 until groupIndex) {
      runningTotal += numberOfVisibleItemsInGroup(i)
    }
    return runningTotal
  }

  /**
   * Converts a child position to a flat list position.
   *
   * @param packedPosition The child positions to be converted in it's
   * packed position representation.
   * @return The flat list position for the given child
   */
  fun getFlattenedChildIndex(packedPosition: Long): Int {
    val listPosition = ExpandableListPosition.obtainPosition(packedPosition)
    return getFlattenedChildIndex(listPosition!!)
  }

  /**
   * Converts a child position to a flat list position.
   *
   * @param listPosition The child positions to be converted in it's
   * [ExpandableListPosition] representation.
   * @return The flat list position for the given child
   */
  fun getFlattenedChildIndex(listPosition: ExpandableListPosition): Int {
    val groupIndex = listPosition.groupPos
    val childIndex = listPosition.childPos
    var runningTotal = 0

    for (i in 0 until groupIndex) {
      runningTotal += numberOfVisibleItemsInGroup(i)
    }
    return runningTotal + childIndex + 1
  }

  /**
   * Converts the details of a child's position to a flat list position.
   *
   * @param groupIndex The index of a group within [.groups]
   * @param childIndex the index of a child within it's [ExpandableGroup]
   * @return The flat list position for the given child
   */
  fun getFlattenedChildIndex(
    groupIndex: Int,
    childIndex: Int
  ): Int {
    var runningTotal = 0

    for (i in 0 until groupIndex) {
      runningTotal += numberOfVisibleItemsInGroup(i)
    }
    return runningTotal + childIndex + 1
  }

  /**
   * @param groupIndex The index of a group within [.groups]
   * @return The flat list position for the first child in a group
   */
  fun getFlattenedFirstChildIndex(groupIndex: Int): Int {
    return getFlattenedGroupIndex(groupIndex) + 1
  }

  /**
   * @param listPosition The child positions to be converted in it's
   * [ExpandableListPosition] representation.
   * @return The flat list position for the first child in a group
   */
  fun getFlattenedFirstChildIndex(listPosition: ExpandableListPosition): Int {
    return getFlattenedGroupIndex(listPosition) + 1
  }

  /**
   * @param listPosition An [ExpandableListPosition] representing either a child or group
   * @return the total number of children within the group associated with the @param listPosition
   */
  fun getExpandableGroupItemCount(listPosition: ExpandableListPosition): Int {
    return groups[listPosition.groupPos].itemCount
  }

  /**
   * Translates either a group pos or a child pos to an [ExpandableGroup].
   * If the [ExpandableListPosition] is a child position, it returns the [ ] it belongs to
   *
   * @param listPosition a [ExpandableListPosition] representing either a group position
   * or child position
   * @return the [ExpandableGroup] object that contains the listPosition
   */
  fun getExpandableGroup(listPosition: ExpandableListPosition): ExpandableGroup<*> {
    return groups[listPosition.groupPos]
  }
}
